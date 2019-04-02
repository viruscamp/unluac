package unluac.assemble;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unluac.decompile.CodeExtract;
import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;
import unluac.decompile.OperandFormat;
import unluac.parse.LHeader;
import unluac.util.StringUtils;

@SuppressWarnings("serial")
class AssemblerException extends Exception {
  
  AssemblerException(String msg) {
    super(msg);
  }
  
}

class AssemblerConstant {
  
  public String name;
  
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
  
  public List<AssemblerConstant> constants;
  
  public AssemblerFunction(AssemblerFunction parent, String name) {
    this.parent = parent;
    this.name = name;
    children = new ArrayList<AssemblerFunction>();
    
    hasSource = false;
    hasLineDefined = false;
    hasLastLineDefined = false;
    hasMaxStackSize = false;
    
    constants = new ArrayList<AssemblerConstant>();
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
    case CONSTANT: {
      String name = a.getName();
      String value = a.getAny();
      
    }
    default:
      throw new IllegalStateException();  
    }
  }
  
  public void processOp(Assembler a, CodeExtract extract, Op op, int opcode) throws AssemblerException, IOException {
    if(!hasMaxStackSize) throw new AssemblerException("Expected .maxstacksize before code");
    if(!extract.check_op(opcode)) throw new IllegalStateException();
    int codepoint = extract.encode_op(opcode);
    for(OperandFormat operand : op.operands) {
      switch(operand) {
      case A: {
        int A = a.getInteger();
        if(!extract.check_A(A)) throw new AssemblerException("Operand A out of range"); 
        codepoint |= extract.encode_A(A);
        break;
      }
      case AR: {
        int r = a.getRegister();
        //TODO: stack warning
        
      }
      default:
        throw new IllegalStateException();
      }
    }
  }
  
}

class AssemblerChunk {
  
  public boolean hasFormat;
  public int format;
  
  public boolean hasEndianness;
  public LHeader.LEndianness endianness;
  
  public boolean hasIntSize;
  public int int_size;
  
  public boolean hasSizeTSize;
  public int size_t_size;
  
  public boolean hasInstructionSize;
  public int instruction_size;
  
  public AssemblerFunction main;
  public AssemblerFunction current;
  
  public AssemblerChunk() {
    hasFormat = false;
    hasEndianness = false;
    hasIntSize = false;
    hasSizeTSize = false;
    hasInstructionSize = false;
    
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
      break;
    case SIZE_T_SIZE:
      if(hasSizeTSize) throw new AssemblerException("Duplicate .size_t_size directive");
      hasSizeTSize = true;
      size_t_size = a.getInteger();
      break;
    case INSTRUCTION_SIZE:
      if(hasInstructionSize) throw new AssemblerException("Duplicate .instruction_size directive");
      hasInstructionSize = true;
      instruction_size = a.getInteger();
      break;
    default:
      throw new IllegalStateException();
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
  MAXSTACKSIZE(".maxstacksize", DirectiveType.FUNCTION, 1),
  CONSTANT(".constant", DirectiveType.FUNCTION, 2),
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
  
  public Assembler(Reader r) {
    t = new Tokenizer(r);
  }
  
  public void assemble() throws AssemblerException, IOException {
    
    String tok = t.next();
    if(tok != ".version") throw new AssemblerException("First directive must be .version");
    tok = t.next();
    if(tok != "5.1") throw new AssemblerException("Only version 5.1 is supported for assembly");
    
    OpcodeMap opmap = new OpcodeMap(0x51);
    Map<String, Op> oplookup = new HashMap<String, Op>();
    for(int i = 0; i < opmap.size(); i++) {
      Op op = opmap.get(i);
      oplookup.put(op.name().toLowerCase(), op);
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
        } else {
          throw new AssemblerException("Unexpected token \"" + "\"");
        }
      }
      
    }
    
    
    
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
  
}
