package unluac.assemble;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import unluac.parse.LAbsLineInfo;
import unluac.parse.LAbsLineInfoType;
import unluac.parse.LBoolean;
import unluac.parse.LBooleanType;
import unluac.parse.LConstantType;
import unluac.parse.LFunction;
import unluac.parse.LFunctionType;
import unluac.parse.LHeader;
import unluac.parse.LLocal;
import unluac.parse.LLocalType;
import unluac.parse.LNil;
import unluac.parse.LNumberType;
import unluac.parse.LNumberType.NumberMode;
import unluac.parse.LObject;
import unluac.parse.LString;
import unluac.parse.LStringType;
import unluac.parse.LUpvalue;
import unluac.parse.LUpvalueType;
import unluac.util.StringUtils;

class AssemblerLabel {
  
  public String name;
  public int code_index;
  
}

class AssemblerConstant {
  
  enum Type {
    NIL,
    BOOLEAN,
    NUMBER,
    INTEGER,
    FLOAT,
    STRING,
    LONGSTRING,
  }
  
  public String name;
  public Type type;
  
  public boolean booleanValue;
  public double numberValue;
  public String stringValue;
  public BigInteger integerValue;
}

class AssemblerAbsLineInfo {
  
  public int pc;
  public int line;
  
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
  
  class FunctionFixup {
    
    int code_index;
    String function;
    CodeExtract.Field field;
    
  }
  
  class JumpFixup {
    
    int code_index;
    String label;
    CodeExtract.Field field;
    boolean negate;
    
  }
  
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
  
  public List<AssemblerLabel> labels;
  public List<AssemblerConstant> constants;
  public List<AssemblerUpvalue> upvalues;
  public List<Integer> code;
  public List<Integer> lines;
  public List<AssemblerAbsLineInfo> abslineinfo;
  public List<AssemblerLocal> locals;
  
  public List<FunctionFixup> f_fixup;
  public List<JumpFixup> j_fixup;
  
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
    
    labels = new ArrayList<AssemblerLabel>();
    constants = new ArrayList<AssemblerConstant>();
    upvalues = new ArrayList<AssemblerUpvalue>();
    code = new ArrayList<Integer>();
    lines = new ArrayList<Integer>();
    abslineinfo = new ArrayList<AssemblerAbsLineInfo>();
    locals = new ArrayList<AssemblerLocal>();
    
    f_fixup = new ArrayList<FunctionFixup>();
    j_fixup = new ArrayList<JumpFixup>();
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
    case LABEL: {
      String name = a.getAny();
      AssemblerLabel label = new AssemblerLabel();
      label.name = name;
      label.code_index = code.size();
      labels.add(label);
      break;
    }
    case CONSTANT: {
      String name = a.getName();
      String value = a.getAny();
      AssemblerConstant constant = new AssemblerConstant();
      constant.name = name;
      if(value.equals("nil")) {
        constant.type = AssemblerConstant.Type.NIL;
      } else if(value.equals("true")) {
        constant.type = AssemblerConstant.Type.BOOLEAN;
        constant.booleanValue = true;
      } else if(value.equals("false")) {
        constant.type = AssemblerConstant.Type.BOOLEAN;
        constant.booleanValue = false;
      } else if(value.startsWith("\"")) {
        constant.type = AssemblerConstant.Type.STRING;
        constant.stringValue = StringUtils.fromPrintString(value);
      } else if(value.startsWith("L\"")) {
        constant.type = AssemblerConstant.Type.LONGSTRING;
        constant.stringValue = StringUtils.fromPrintString(value.substring(1));
      } else if(value.equals("null")) {
        constant.type = AssemblerConstant.Type.STRING;
        constant.stringValue = null;
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
    case ABSLINEINFO: {
      AssemblerAbsLineInfo info = new AssemblerAbsLineInfo();
      info.pc = a.getInteger();
      info.line = a.getInteger();
      abslineinfo.add(info);
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
    if(opcode >= 0 && !extract.op.check(opcode)) throw new IllegalStateException("Invalid opcode: " + opcode);
    int codepoint = opcode >= 0 ? extract.op.encode(opcode) : 0;
    for(OperandFormat operand : op.operands) {
      CodeExtract.Field field;
      switch(operand.field) {
      case A: field = extract.A; break;
      case B: field = extract.B; break;
      case C: field = extract.C; break;
      case k: field = extract.k; break;
      case Ax: field = extract.Ax; break;
      case sJ: field = extract.sJ; break;
      case Bx: field = extract.Bx; break;
      case sBx: field = extract.sBx; break;
      case x: field = extract.x; break;
      default: throw new IllegalStateException("Unhandled field: " + operand.field);
      }
      int x;
      switch(operand.format) {
      case RAW:
      case IMMEDIATE_INTEGER:
      case IMMEDIATE_FLOAT:
        x = a.getInteger();
        break;
      case IMMEDIATE_SIGNED_INTEGER:
        x = a.getInteger();
        x += field.max() / 2;
        break;
      case REGISTER: {
        x = a.getRegister();
        //TODO: stack warning
        break;
      }
      case REGISTER_K: {
        Assembler.RKInfo rk = a.getRegisterK54();
        x = rk.x;
        if(rk.constant) {
          x += chunk.version.rkoffset.get();
        }
        //TODO: stack warning
        break;
      }
      case REGISTER_K54: {
        Assembler.RKInfo rk = a.getRegisterK54();
        codepoint |= extract.k.encode(rk.constant ? 1 : 0);
        x = rk.x;
        break;
      }
      case CONSTANT:
      case CONSTANT_INTEGER:
      case CONSTANT_STRING: {
        x = a.getConstant();
        break;
      }
      case UPVALUE: {
        x = a.getUpvalue();
        break;
      }
      case FUNCTION: {
        FunctionFixup fix = new FunctionFixup();
        fix.code_index = code.size();
        fix.function = a.getAny();
        fix.field = field;
        f_fixup.add(fix);
        x = 0;
        break;
      }
      case JUMP: {
        JumpFixup fix = new JumpFixup();
        fix.code_index = code.size();
        fix.label = a.getAny();
        fix.field = field;
        fix.negate = false;
        j_fixup.add(fix);
        x = 0;
        break;
      }
      case JUMP_NEGATIVE: {
        JumpFixup fix = new JumpFixup();
        fix.code_index = code.size();
        fix.label = a.getAny();
        fix.field = field;
        fix.negate = true;
        j_fixup.add(fix);
        x = 0;
        break;
      }
      default:
        throw new IllegalStateException("Unhandled operand format: " + operand.format);
      }
      if(!field.check(x)) {
        throw new AssemblerException("Operand " + operand.field + " out of range"); 
      }
      codepoint |= field.encode(x);
    }
    code.add(codepoint);
  }
  
  public void fixup(CodeExtract extract) throws AssemblerException {
    for(FunctionFixup fix : f_fixup) {
      int codepoint = code.get(fix.code_index);
      int x = -1;
      for(int f = 0; f < children.size(); f++) {
        AssemblerFunction child = children.get(f);
        if(fix.function.equals(child.name)) {
          x = f;
          break;
        }
      }
      if(x == -1) {
        throw new AssemblerException("Unknown function: " + fix.function);
      }
      codepoint = fix.field.clear(codepoint);
      codepoint |= fix.field.encode(x);
      code.set(fix.code_index, codepoint);
    }
    
    for(JumpFixup fix : j_fixup) {
      int codepoint = code.get(fix.code_index);
      int x = 0;
      boolean found = false;
      for(AssemblerLabel label : labels) {
        if(fix.label.equals(label.name)) {
          x = label.code_index - fix.code_index - 1;
          if(fix.negate) x = -x;
          found = true;
          break;
        }
      }
      if(!found) {
        throw new AssemblerException("Unknown label: " + fix.label);
      }
      codepoint = fix.field.clear(codepoint);
      codepoint |= fix.field.encode(x);
      code.set(fix.code_index, codepoint);
    }
    
    for(AssemblerFunction f : children) {
      f.fixup(extract);
    }
  }
  
}

class AssemblerChunk {
  
  public Version version;
  
  public int format;
  
  public LHeader.LEndianness endianness;
  
  public int int_size;
  public BIntegerType integer;
  
  public int size_t_size;
  public BIntegerType sizeT;
  
  public int instruction_size;
  public int op_size;
  public int a_size;
  public int b_size;
  public int c_size;
  
  public Map<Integer, Op> useropmap;
  
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
    if(d != Directive.OP && processed_directives.contains(d)) {
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
      integer = BIntegerType.create50Type(int_size);
      break;
    case SIZE_T_SIZE:
      size_t_size = a.getInteger();
      sizeT = BIntegerType.create50Type(size_t_size);
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
    case OP: {
      if(useropmap == null) {
        useropmap = new HashMap<Integer, Op>();
      }
      int opcode = a.getInteger();
      String name = a.getName();
      Op op = version.getOpcodeMap().get(name);
      if(op == null) {
        throw new AssemblerException("Unknown op name \"" + name + "\"");
      }
      useropmap.put(opcode, op);
      break;
    }
    default:
      throw new IllegalStateException("Unhandled directive: " + d);
    }
  }
  
  public CodeExtract getCodeExtract() throws AssemblerException {
    if(extract == null) {
      extract = new CodeExtract(version, op_size, a_size, b_size, c_size);
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
  
  public void fixup() throws AssemblerException {
    main.fixup(getCodeExtract());
  }
  
  public void write(OutputStream out) throws AssemblerException, IOException {
    LBooleanType bool = new LBooleanType();
    LStringType string = version.getLStringType();
    LConstantType constant = version.getLConstantType();
    LAbsLineInfoType abslineinfo = new LAbsLineInfoType();
    LLocalType local = new LLocalType();
    LUpvalueType upvalue = version.getLUpvalueType();
    LFunctionType function = version.getLFunctionType();
    CodeExtract extract = getCodeExtract();
    
    if(integer == null) {
      integer = BIntegerType.create54();
      sizeT = integer;
    }
    
    LHeader lheader = new LHeader(format, endianness, integer, sizeT, bool, number, linteger, lfloat, string, constant, abslineinfo, local, upvalue, function, extract);
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
    int[] lines = new int[function.lines.size()];
    i = 0;
    for(int line : function.lines) {
      lines[i++] = line;
    }
    LAbsLineInfo[] abslineinfo = new LAbsLineInfo[function.abslineinfo.size()];
    i = 0;
    for(AssemblerAbsLineInfo info : function.abslineinfo) {
      abslineinfo[i++] = new LAbsLineInfo(info.pc, info.line);
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
      case NIL:
        object = LNil.NIL;
        break;
      case BOOLEAN:
        object = constant.booleanValue ? LBoolean.LTRUE : LBoolean.LFALSE;
        break;
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
      case LONGSTRING:
        object = convert_long_string(header, constant.stringValue);
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
      lines,
      abslineinfo,
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
    if(string == null) {
      return LString.NULL;
    } else {
      return new LString(string);
    }
  }
  
  private LString convert_long_string(BHeader header, String string) {
    return new LString(string, true);
  }

}

public class Assembler {

  private Tokenizer t;
  private OutputStream out;
  private Version version;
  
  public Assembler(InputStream in, OutputStream out) {
    t = new Tokenizer(in);
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
    
    version = Version.getVersion(major, minor);
    
    if(version == null) {
      throw new AssemblerException("Unsupported version " + tok);
    }
    
    Map<String, Op> oplookup = null;
    Map<Op, Integer> opcodelookup = null;
    
    AssemblerChunk chunk = new AssemblerChunk(version);
    boolean opinit = false;
    
    while((tok = t.next()) != null) {
      Directive d = Directive.lookup.get(tok);
      if(d != null) {
        switch(d.type) {
        case HEADER:
          chunk.processHeaderDirective(this, d);
          break;
        case NEWFUNCTION:
          if(!opinit) {
            opinit = true;
            OpcodeMap opmap;
            if(chunk.useropmap != null) {
              opmap = new OpcodeMap(chunk.useropmap);
            } else {
              opmap = version.getOpcodeMap();
            }
            oplookup = new HashMap<String, Op>();
            opcodelookup = new HashMap<Op, Integer>();
            for(int i = 0; i < opmap.size(); i++) {
              Op op = opmap.get(i);
              if(op != null) {
                oplookup.put(op.name, op);
                opcodelookup.put(op, i);
              }
            }
            
            oplookup.put(Op.EXTRABYTE.name, Op.EXTRABYTE);
            opcodelookup.put(Op.EXTRABYTE, -1);
          }
          
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
    
    chunk.fixup();
    
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
  
  static class RKInfo {
    int x;
    boolean constant;
  }
  
  RKInfo getRegisterK54() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    RKInfo rk = new RKInfo();
    if(s.length() >= 2 && s.charAt(0) == 'r') {
      rk.constant = false;
      try {
        rk.x = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted register, got \"" + s + "\"");
      }
    } else if(s.length() >= 2 && s.charAt(0) == 'k') {
      rk.constant = true;
      try {
        rk.x = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted constant, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted register or constant, got \"" + s + "\"");
    }
    return rk;
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
  
  int getUpvalue() throws AssemblerException, IOException {
    String s = t.next();
    if(s == null) throw new AssemblerException("Unexcepted end of file");
    int u;
    if(s.length() >= 2 && s.charAt(0) == 'u') {
      try {
        u = Integer.parseInt(s.substring(1));
      } catch(NumberFormatException e) {
        throw new AssemblerException("Excepted register, got \"" + s + "\"");
      }
    } else {
      throw new AssemblerException("Excepted register, got \"" + s + "\"");
    }
    return u;
  }
  
}
