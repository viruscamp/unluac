package unluac.assemble;

import java.io.OutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unluac.Version;
import unluac.decompile.CodeExtract;
import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;
import unluac.decompile.OperandFormat;
import unluac.parse.BHeader;
import unluac.parse.BInteger;
import unluac.parse.BIntegerType;
import unluac.parse.BList;
import unluac.parse.BSizeT;
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
    NUMBER(3),
    STRING(4);
    
    Type(int code) {
      this.code = code;
    }
    
    int code;
  }
  
  public String name;
  public Type type;
  
  public double numberValue;
  public String stringValue;
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
  
  public AssemblerFunction(AssemblerFunction parent, String name) {
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
    AssemblerFunction child = new AssemblerFunction(this, name);
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
          constant.numberValue = Double.parseDouble(value);
          constant.type = AssemblerConstant.Type.NUMBER;
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
      upvalue.name = a.getName();
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
  
  public boolean hasFormat;
  public int format;
  
  public boolean hasEndianness;
  public LHeader.LEndianness endianness;
  
  public boolean hasIntSize;
  public int int_size;
  public BIntegerType integer;
  
  public boolean hasSizeTSize;
  public int size_t_size;
  public BSizeTType sizeT;
  
  public boolean hasInstructionSize;
  public int instruction_size;
  
  public boolean hasNumberFormat;
  public boolean number_integral;
  public int number_size;
  public LNumberType number;
  
  public AssemblerFunction main;
  public AssemblerFunction current;
  
  public AssemblerChunk() {
    hasFormat = false;
    hasEndianness = false;
    hasIntSize = false;
    hasSizeTSize = false;
    hasInstructionSize = false;
    hasNumberFormat = false;
    
    main = null;
    current = null;
  }
  
  public void processHeaderDirective(Assembler a, Directive d) throws AssemblerException, IOException {
    switch(d) {
    case FORMAT:
      if(hasFormat) throw new AssemblerException("Duplicate .format directive");
      hasFormat = true;
      format = a.getInteger();
      break;
    case ENDIANNESS: {
      if(hasEndianness) throw new AssemblerException("Duplicate .endianness directive");
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
      if(hasIntSize) throw new AssemblerException("Duplicate .int_size directive");
      hasIntSize = true;
      int_size = a.getInteger();
      integer = new BIntegerType(int_size);
      break;
    case SIZE_T_SIZE:
      if(hasSizeTSize) throw new AssemblerException("Duplicate .size_t_size directive");
      hasSizeTSize = true;
      size_t_size = a.getInteger();
      sizeT = new BSizeTType(size_t_size);
      break;
    case INSTRUCTION_SIZE:
      if(hasInstructionSize) throw new AssemblerException("Duplicate .instruction_size directive");
      hasInstructionSize = true;
      instruction_size = a.getInteger();
      break;
    case NUMBER_FORMAT: {
      if(hasNumberFormat) throw new AssemblerException("Duplicate .number_format directive");
      hasNumberFormat = true;
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
    default:
      throw new IllegalStateException("Unhandled directive: " + d);
    }
  }
  
  public void processNewFunction(Assembler a) throws AssemblerException, IOException {
    String name = a.getName();
    String[] parts = name.split("/");
    if(main == null) {
      if(parts.length != 1) throw new AssemblerException("First (main) function declaration must not have a \"/\" in the name");
      main = new AssemblerFunction(null, name);
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
  
  public void processOp(Assembler a, CodeExtract ex, Op op, int opcode) throws AssemblerException, IOException {
    if(current == null) {
      throw new AssemblerException("Misplaced code before declaration of any function");
    }
    current.processOp(a, ex, op, opcode);
  }
  
  public void write(OutputStream out) throws IOException {
    Version version = Version.LUA51;
    LBooleanType bool = new LBooleanType();
    LStringType string = LStringType.getType50(version);
    LConstantType constant = LConstantType.getType50();
    LLocalType local = new LLocalType();
    LUpvalueType upvalue = new LUpvalueType();
    LFunctionType function = LFunctionType.TYPE51;
    CodeExtract extract = new CodeExtract(version); // TODO: 
    
    LHeader lheader = new LHeader(format, endianness, integer, sizeT, bool, number, null, null, string, constant, local, upvalue, function, extract);
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
    // TODO:
    return new LString(header.version, new BSizeT(string.length() + 1), string.concat("\0"));
  }

}

enum DirectiveType {
  HEADER,
  NEWFUNCTION,
  FUNCTION,
  INSTRUCTION;
}

enum Directive {
  FORMAT(".format", DirectiveType.HEADER, 1),
  ENDIANNESS(".endianness", DirectiveType.HEADER, 1),
  INT_SIZE(".int_size", DirectiveType.HEADER, 1),
  SIZE_T_SIZE(".size_t_size", DirectiveType.HEADER, 1),
  INSTRUCTION_SIZE(".instruction_size", DirectiveType.HEADER, 1),
  NUMBER_FORMAT(".number_format", DirectiveType.HEADER, 2),
  FUNCTION(".function", DirectiveType.NEWFUNCTION, 1),
  SOURCE(".source", DirectiveType.FUNCTION, 1),
  LINEDEFINED(".linedefined", DirectiveType.FUNCTION, 1),
  LASTLINEDEFINED(".lastlinedefined", DirectiveType.FUNCTION, 1),
  NUMPARAMS(".numparams", DirectiveType.FUNCTION, 1),
  IS_VARARG(".is_vararg", DirectiveType.FUNCTION, 1),
  MAXSTACKSIZE(".maxstacksize", DirectiveType.FUNCTION, 1),
  CONSTANT(".constant", DirectiveType.FUNCTION, 2),
  LINE(".line", DirectiveType.FUNCTION, 1),
  LOCAL(".local", DirectiveType.FUNCTION, 3),
  UPVALUE(".upvalue", DirectiveType.FUNCTION, 2),
  ;
  Directive(String token, DirectiveType type, int argcount) {
    this.token = token;
    this.type = type;
  }
  
  public final String token;
  public final DirectiveType type;
  
  static Map<String, Directive> lookup;
  
  static {
    lookup = new HashMap<String, Directive>();
    for(Directive d : Directive.values()) {
      lookup.put(d.token, d);
    }
  }
}

public class Assembler {

  private Tokenizer t;
  private OutputStream out;
  
  public Assembler(Reader r, OutputStream out) {
    t = new Tokenizer(r);
    this.out = out;
  }
  
  public void assemble() throws AssemblerException, IOException {
    
    String tok = t.next();
    if(!tok.equals(".version")) throw new AssemblerException("First directive must be .version, instead was \"" + tok + "\"");
    tok = t.next();
    if(!tok.equals("5.1")) throw new AssemblerException("Only version 5.1 is supported for assembly");
    
    OpcodeMap opmap = new OpcodeMap(0x51);
    Map<String, Op> oplookup = new HashMap<String, Op>();
    Map<Op, Integer> opcodelookup = new HashMap<Op, Integer>();
    for(int i = 0; i < opmap.size(); i++) {
      Op op = opmap.get(i);
      oplookup.put(op.name().toLowerCase(), op);
      opcodelookup.put(op, i);
    }
    
    AssemblerChunk chunk = new AssemblerChunk();
    
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
          chunk.processOp(this, new CodeExtract(Version.LUA51), op, opcodelookup.get(op));
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
