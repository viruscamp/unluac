package unluac.assemble;

import java.io.OutputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unluac.Version;
import unluac.decompile.CodeExtract;
import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;
import unluac.decompile.OperandFormat;
import unluac.parse.BHeader;
import unluac.parse.BInteger;
import unluac.parse.BIntegerType;
import unluac.parse.BList;
import unluac.parse.BSizeTType;
import unluac.parse.LBooleanType;
import unluac.parse.LConstantType;
import unluac.parse.LFunction;
import unluac.parse.LFunctionType;
import unluac.parse.LHeader;
import unluac.parse.LLocal;
import unluac.parse.LLocalType;
import unluac.parse.LNumberType;
import unluac.parse.LNumberType.NumberMode;
import unluac.parse.LObject;
import unluac.parse.LString;
import unluac.parse.LStringType;
import unluac.parse.LUpvalue;
import unluac.parse.LUpvalueType;
import unluac.util.StringUtils;

class AssemblerConstant {
  
  enum Type {
    NUMBER,
    INTEGER,
    FLOAT,
    STRING,
  }
  
  public String name;
  public Type type;
  
  public double numberValue;
  public String stringValue;
  public BigInteger integerValue;
}

class AssemblerLocal {
  
  public String name;
  public int begin;
  public int end;
  
}

class AssemblerUpvalue {
  
  public String name;
  public int index;
  public boolean instack;
  
}

class AssemblerFunction {
  
  public AssemblerChunk chunk;
  public AssemblerFunction parent;
  public String name;
  public List<AssemblerFunction> children;
  
  public boolean hasSource;
  public String source;
  
  public boolean hasLineDefined;
  public int linedefined;
  
  public boolean hasLastLineDefined;
  public int lastlinedefined;
  
  public boolean hasMaxStackSize;
  public int maxStackSize;
  
  public boolean hasNumParams;
  public int numParams;
  
  public boolean hasVararg;
  public int vararg;
  
  public List<AssemblerConstant> constants;
  public List<AssemblerUpvalue> upvalues;
  public List<Integer> code;
  public List<Integer> lines;
  public List<AssemblerLocal> locals;
  
  public AssemblerFunction(AssemblerChunk chunk, AssemblerFunction parent, String name) {
    this.chunk = chunk;
    this.parent = parent;
    this.name = name;
    children = new ArrayList<AssemblerFunction>();
    
    hasSource = false;
    hasLineDefined = false;
    hasLastLineDefined = false;
    hasMaxStackSize = false;
    hasNumParams = false;
    hasVararg = false;
    
    constants = new ArrayList<AssemblerConstant>();
    upvalues = new ArrayList<AssemblerUpvalue>();
    code = new ArrayList<Integer>();
    lines = new ArrayList<Integer>();
    locals = new ArrayList<AssemblerLocal>();
  }
  
  public AssemblerFunction addChild(String name) {
    AssemblerFunction child = new AssemblerFunction(chunk, this, name);
    children.add(child);
    return child;
  }
  
  public AssemblerFunction getInnerParent(String[] parts, int index) throws AssemblerException {
    if(index + 1 == parts.length) return this;
    for(AssemblerFunction child : children) {
      if(child.name.equals(parts[index])) {
        return child.getInnerParent(parts, index + 1);
      }
    }
    throw new AssemblerException("Can't find outer function");
  }
  
  public void processFunctionDirective(Assembler a, Directive d) throws AssemblerException, IOException {
    switch(d) {
    case SOURCE:
      if(hasSource) throw new AssemblerException("Duplicate .source directive");
      hasSource = true;
      source = a.getString();
      break;
    case LINEDEFINED:
      if(hasLineDefined) throw new AssemblerException("Duplicate .linedefined directive");
      hasLineDefined = true;
      linedefined = a.getInteger();
      break;
    case LASTLINEDEFINED:
      if(hasLastLineDefined) throw new AssemblerException("Duplicate .lastlinedefined directive");
      hasLastLineDefined = true;
      lastlinedefined = a.getInteger();
      break;
    case MAXSTACKSIZE:
      if(hasMaxStackSize) throw new AssemblerException("Duplicate .maxstacksize directive");
      hasMaxStackSize = true;
      maxStackSize = a.getInteger();
      break;
    case NUMPARAMS:
      if(hasNumParams) throw new AssemblerException("Duplicate .numparams directive");
      hasNumParams = true;
      numParams = a.getInteger();
      break;
    case IS_VARARG:
      if(hasVararg) throw new AssemblerException("Duplicate .is_vararg directive");
      hasVararg = true;
      vararg = a.getInteger();
      break;
    case CONSTANT: {
      String name = a.getName();
      String value = a.getAny();
      AssemblerConstant constant = new AssemblerConstant();
      constant.name = name;
      if(value.startsWith("\"")) {
        constant.type = AssemblerConstant.Type.STRING;
        constant.stringValue = StringUtils.fromPrintString(value);
      } else {
        try {
          // TODO: better check
          if(chunk.number != null) {
            constant.numberValue = Double.parseDouble(value);
            constant.type = AssemblerConstant.Type.NUMBER;
          } else {
            if(value.contains(".") || value.contains("E") || value.contains("e")) {
              constant.numberValue = Double.parseDouble(value);
              constant.type = AssemblerConstant.Type.FLOAT;
            } else {
              constant.integerValue = new BigInteger(value);
              constant.type = AssemblerConstant.Type.INTEGER;
            }
          }
        } catch(NumberFormatException e) {
          throw new IllegalStateException("Unrecognized constant value: " + value);
        }
      }
      constants.add(constant);
      break;
    }
    case LINE: {
      lines.add(a.getInteger());
      break;
    }
    case LOCAL: {
      AssemblerLocal local = new AssemblerLocal();
      local.name = a.getString();
      local.begin = a.getInteger();
      local.end = a.getInteger();
      locals.add(local);
      break;
    }
    case UPVALUE: {
      AssemblerUpvalue upvalue = new AssemblerUpvalue();
      upvalue.name = a.getString();
      upvalue.index = a.getInteger();
      upvalue.instack = a.getBoolean();
      upvalues.add(upvalue);
      break;
    }
    default:
      throw new IllegalStateException("Unhandled directive: " + d);  
    }
  }
  
  public void processOp(Assembler a, CodeExtract extract, Op op, int opcode) throws AssemblerException, IOException {
    if(!hasMaxStackSize) throw new AssemblerException("Expected .maxstacksize before code");
    if(!extract.op.check(opcode)) throw new IllegalStateException();
    int codepoint = extract.op.encode(opcode);
    for(OperandFormat operand : op.operands) {
      switch(operand) {
      case A: {
        int A = a.getInteger();
        if(!extract.A.check(A)) throw new AssemblerException("Operand A out of range"); 
        codepoint |= extract.A.encode(A);
        break;
      }
      case AR: {
        int r = a.getRegister();
        //TODO: stack warning
        if(!extract.A.check(r)) throw new AssemblerException("Operand A out of range");
        codepoint |= extract.A.encode(r);
        break;
      }
      case B: {
        int B = a.getInteger();
        if(!extract.B.check(B)) throw new AssemblerException("Operand B out of range"); 
        codepoint |= extract.B.encode(B);
        break;
      }
      case BxK: {
        int Bx = a.getConstant();
        if(!extract.Bx.check(Bx)) throw new AssemblerException("Operand Bx out of range");
        codepoint |= extract.Bx.encode(Bx);
        break;
      }
      
      default:
        throw new IllegalStateException("Unhandled operand format: " + operand);
      }
    }
    code.add(codepoint);
  }
  
}

class AssemblerChunk {
  
  public Version version;
  
  public int format;
  
  public LHeader.LEndianness endianness;
  
  public int int_size;
  public BIntegerType integer;
  
  public int size_t_size;
  public BSizeTType sizeT;
  
  public int instruction_size;
  public int op_size;
  public int a_size;
  public int b_size;
  public int c_size;
  
  public boolean number_integral;
  public int number_size;
  public LNumberType number;
  
  public LNumberType linteger;
  
  public LNumberType lfloat;
  
  public AssemblerFunction main;
  public AssemblerFunction current;
  public CodeExtract extract;
  
  public final Set<Directive> processed_directives;
  
  public AssemblerChunk(Version version) {
    this.version = version;
    processed_directives = new HashSet<Directive>();
    
    main = null;
    current = null;
    extract = null;
  }
  
  public void processHeaderDirective(Assembler a, Directive d) throws AssemblerException, IOException {
    if(processed_directives.contains(d)) {
      throw new AssemblerException("Duplicate " + d.name() + " directive");
    }
    processed_directives.add(d);
    switch(d) {
    case FORMAT:
      format = a.getInteger();
      break;
    case ENDIANNESS: {
      String endiannessName = a.getName();
      switch(endiannessName) {
      case "LITTLE":
        endianness = LHeader.LEndianness.LITTLE;
        break;
      case "BIG":
        endianness = LHeader.LEndianness.BIG;
        break;
      default:
        throw new AssemblerException("Unknown endianness \"" + endiannessName + "\"");
      }
      break;
    }
    case INT_SIZE:
      int_size = a.getInteger();
      integer = new BIntegerType(int_size);
      break;
    case SIZE_T_SIZE:
      size_t_size = a.getInteger();
      sizeT = new BSizeTType(size_t_size);
      break;
    case INSTRUCTION_SIZE:
      instruction_size = a.getInteger();
      break;
    case SIZE_OP:
      op_size = a.getInteger();
      break;
    case SIZE_A:
      a_size = a.getInteger();
      break;
    case SIZE_B:
      b_size = a.getInteger();
      break;
    case SIZE_C:
      c_size = a.getInteger();
      break;
    case NUMBER_FORMAT: {
      String numberTypeName = a.getName();
      switch(numberTypeName) {
      case "integer": number_integral = true; break;
      case "float": number_integral = false; break;
      default: throw new AssemblerException("Unknown number_format \"" + numberTypeName + "\"");
      }
      number_size = a.getInteger();
      number = new LNumberType(number_size, number_integral, NumberMode.MODE_NUMBER);
      break;
    }
    case INTEGER_FORMAT:
      linteger = new LNumberType(a.getInteger(), true, NumberMode.MODE_INTEGER);
      break;
    case FLOAT_FORMAT:
      lfloat = new LNumberType(a.getInteger(), false, NumberMode.MODE_FLOAT);
      break;
    default:
      throw new IllegalStateException("Unhandled directive: " + d);
    }
  }
  
  public CodeExtract getCodeExtract() throws AssemblerException {
    if(extract == null) {
      // TODO: better checking
      if(processed_directives.contains(Directive.SIZE_OP)) {
        extract = new CodeExtract(version, op_size, a_size, b_size, c_size);
      } else {
        extract = new CodeExtract(version);
      }
    }
    return extract;
  }
  
  public void processNewFunction(Assembler a) throws AssemblerException, IOException {
    String name = a.getName();
    String[] parts = name.split("/");
    if(main == null) {
      if(parts.length != 1) throw new AssemblerException("First (main) function declaration must not have a \"/\" in the name");
      main = new AssemblerFunction(this, null, name);
      current = main;
    } else {
      if(parts.length == 1 || !parts[0].equals(main.name)) throw new AssemblerException("Function \"" + name + "\" isn't contained in the main function");
      AssemblerFunction parent = main.getInnerParent(parts, 1);
      current = parent.addChild(parts[parts.length - 1]);
    }
  }
  
  public void processFunctionDirective(Assembler a, Directive d) throws AssemblerException, IOException {
    if(current == null) {
      throw new AssemblerException("Misplaced function directive before declaration of any function");
    }
    current.processFunctionDirective(a, d);
  }
  
  public void processOp(Assembler a, Op op, int opcode) throws AssemblerException, IOException {
    if(current == null) {
      throw new AssemblerException("Misplaced code before declaration of any function");
    }
    current.processOp(a, getCodeExtract(), op, opcode);
  }
  
  public void write(OutputStream out) throws AssemblerException, IOException {
    LBooleanType bool = new LBooleanType();
    LStringType string = LStringType.get(version);
    LConstantType constant = LConstantType.get(version);
    LLocalType local = new LLocalType();
    LUpvalueType upvalue = new LUpvalueType();
    LFunctionType function = LFunctionType.get(version);
    CodeExtract extract = getCodeExtract();
    
    LHeader lheader = new LHeader(format, endianness, integer, sizeT, bool, number, linteger, lfloat, string, constant, local, upvalue, function, extract);
    BHeader header = new BHeader(version, lheader);
    LFunction main = convert_function(header, this.main);
    header = new BHeader(version, lheader, main);
    
    header.write(out);
  }
  
  private LFunction convert_function(BHeader header, AssemblerFunction function) {
    int i;
    int[] code = new int[function.code.size()];
    i = 0;
    for(int codepoint : function.code) {
      code[i++] = codepoint;
    }
    ArrayList<BInteger> lines = new ArrayList<BInteger>(function.lines.size());
    for(int line : function.lines) {
      lines.add(new BInteger(line));
    }
    LLocal[] locals = new LLocal[function.locals.size()];
    i = 0;
    for(AssemblerLocal local : function.locals) {
      locals[i++] = new LLocal(convert_string(header, local.name), new BInteger(local.begin), new BInteger(local.end));
    }
    LObject[] constants = new LObject[function.constants.size()];
    i = 0;
    for(AssemblerConstant constant : function.constants) {
      LObject object;
      switch(constant.type) {
      case NUMBER:
        object = header.number.create(constant.numberValue);
        break;
      case INTEGER:
        object = header.linteger.create(constant.integerValue);
        break;
      case FLOAT:
        object = header.lfloat.create(constant.numberValue);
        break;
      case STRING:
        object = convert_string(header, constant.stringValue);
        break;
      default:
        throw new IllegalStateException();
      }
      constants[i++] = object;
    }
    LUpvalue[] upvalues = new LUpvalue[function.upvalues.size()];
    i = 0;
    for(AssemblerUpvalue upvalue : function.upvalues) {
      LUpvalue lup = new LUpvalue();
      lup.bname = convert_string(header, upvalue.name);
      lup.idx = upvalue.index;
      lup.instack = upvalue.instack;
      upvalues[i++] = lup;
    }
    LFunction[] functions = new LFunction[function.children.size()];
    i = 0;
    for(AssemblerFunction f : function.children) {
      functions[i++] = convert_function(header, f);
    }
    return new LFunction(
      header,
      convert_string(header, function.source),
      function.linedefined,
      function.lastlinedefined,
      code,
      new BList<BInteger>(new BInteger(function.lines.size()), lines),
      locals,
      constants,
      upvalues,
      functions,
      function.maxStackSize,
      function.upvalues.size(),
      function.numParams,
      function.vararg
   );
  }
  
  private LString convert_string(BHeader header, String string) {
    return new LString(header.version, string);
  }

}

public class Assembler {

  private Tokenizer t;
  private OutputStream out;
  private Version version;
  
  public Assembler(Reader r, OutputStream out) {
    t = new Tokenizer(r);
    this.out = out;
  }
  
  public void assemble() throws AssemblerException, IOException {
    
    String tok = t.next();
    if(!tok.equals(".version")) throw new AssemblerException("First directive must be .version, instead was \"" + tok + "\"");
    tok = t.next();
    
    int major;
    int minor;
    String[] parts = tok.split("\\.");
    if(parts.length == 2) {
      try {
        major = Integer.valueOf(parts[0]);
        minor = Integer.valueOf(parts[1]);
      } catch(NumberFormatException e) {
        throw new AssemblerException("Unsupported version " + tok);
      }
    } else {
      throw new AssemblerException("Unsupported version " + tok);
    }
    if(major < 0 || major > 0xF || minor < 0 || minor > 0xF) {
      throw new AssemblerException("Unsupported version " + tok);
    }
    
    int version_number = (major << 4) | minor;
    version = Version.getVersion(version_number);
    
    if(version == null) {
      throw new AssemblerException("Unsupported version " + tok);
    }
    
    OpcodeMap opmap = new OpcodeMap(version_number);
    Map<String, Op> oplookup = new HashMap<String, Op>();
    Map<Op, Integer> opcodelookup = new HashMap<Op, Integer>();
    for(int i = 0; i < opmap.size(); i++) {
      Op op = opmap.get(i);
      oplookup.put(op.name().toLowerCase(), op);
      opcodelookup.put(op, i);
    }
    
    AssemblerChunk chunk = new AssemblerChunk(version);
    
    while((tok = t.next()) != null) {
      Directive d = Directive.lookup.get(tok);
      if(d != null) {
        switch(d.type) {
        case HEADER:
          chunk.processHeaderDirective(this, d);
          break;
        case NEWFUNCTION:
          chunk.processNewFunction(this);
          break;
        case FUNCTION:
          chunk.processFunctionDirective(this, d);
          break;
        default:
          throw new IllegalStateException();
        }
        
      } else {
        Op op = oplookup.get(tok);
        if(op != null) {
          // TODO:
          chunk.processOp(this, op, opcodelookup.get(op));
        } else {
          throw new AssemblerException("Unexpected token \"" + tok + "\"");
        }
      }
      
    }
    
    chunk.write(out);
    
  }
  
  String getAny() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    return s;
  }
  
  String getName() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    return s;
  }
  
  String getString() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    return StringUtils.fromPrintString(s);
  }
  
  int getInteger() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    int i;
    try {
      i = Integer.parseInt(s);
    } catch(NumberFormatException e) {
      throw new AssemblerException("Excepted number, got \"" + s + "\"");
    }
    return i;
  }
  
  boolean getBoolean() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    boolean b;
    if(s.equals("true")) {
      b = true;
    } else if(s.equals("false")) {
      b = false;
    } else {
      throw new AssemblerException("Expected boolean, got \"" + s + "\"");
    }
    return b;
  }
  
  int getRegister() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    int r;
    if(s.length() >= 2 && s.charAt(0) == 'r') {
      try {
        r = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted register, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted register, got \"" + s + "\"");
    }
    return r;
  }
  
  int getConstant() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexpected end of file");
    int k;
    if(s.length() >= 2 && s.charAt(0) == 'k') {
      try {
        k = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted constant, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted constant, got \"" + s + "\"");
    }
    return k;
  }
  
}
