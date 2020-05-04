package unluac.assemble;

import java.util.HashMap;
import java.util.Map;

import unluac.decompile.Output;
import unluac.parse.BHeader;
import unluac.parse.LFunction;
import unluac.parse.LHeader;
import unluac.util.StringUtils;

enum DirectiveType {
  HEADER,
  NEWFUNCTION,
  FUNCTION,
  INSTRUCTION;
}

public enum Directive {
  FORMAT(".format", DirectiveType.HEADER, 1),
  ENDIANNESS(".endianness", DirectiveType.HEADER, 1),
  INT_SIZE(".int_size", DirectiveType.HEADER, 1),
  SIZE_T_SIZE(".size_t_size", DirectiveType.HEADER, 1),
  INSTRUCTION_SIZE(".instruction_size", DirectiveType.HEADER, 1),
  SIZE_OP(".size_op", DirectiveType.HEADER, 1),
  SIZE_A(".size_a", DirectiveType.HEADER, 1),
  SIZE_B(".size_b", DirectiveType.HEADER, 1),
  SIZE_C(".size_c", DirectiveType.HEADER, 1),
  NUMBER_FORMAT(".number_format", DirectiveType.HEADER, 2),
  INTEGER_FORMAT(".integer_format", DirectiveType.HEADER, 1),
  FLOAT_FORMAT(".float_format", DirectiveType.HEADER, 1),
  FUNCTION(".function", DirectiveType.NEWFUNCTION, 1),
  SOURCE(".source", DirectiveType.FUNCTION, 1),
  LINEDEFINED(".linedefined", DirectiveType.FUNCTION, 1),
  LASTLINEDEFINED(".lastlinedefined", DirectiveType.FUNCTION, 1),
  NUMPARAMS(".numparams", DirectiveType.FUNCTION, 1),
  IS_VARARG(".is_vararg", DirectiveType.FUNCTION, 1),
  MAXSTACKSIZE(".maxstacksize", DirectiveType.FUNCTION, 1),
  LABEL(".label", DirectiveType.FUNCTION, 1),
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
  
  public void disassemble(Output out, BHeader chunk, LHeader header) {
    out.print(this.token + "\t");
    switch(this) {
    case FORMAT: out.println(String.valueOf(header.format)); break;
    case ENDIANNESS: out.println(header.endianness.toString()); break;
    case INT_SIZE: out.println(String.valueOf(header.integer.getSize())); break;
    case SIZE_T_SIZE: out.println(String.valueOf(header.sizeT.getSize())); break;
    case INSTRUCTION_SIZE: out.println("4"); break;
    case SIZE_OP: out.println(String.valueOf(header.extractor.op.size)); break;
    case SIZE_A: out.println(String.valueOf(header.extractor.A.size)); break;
    case SIZE_B: out.println(String.valueOf(header.extractor.B.size)); break;
    case SIZE_C: out.println(String.valueOf(header.extractor.C.size)); break;
    case NUMBER_FORMAT: out.println((header.number.integral ? "integer" : "float") + "\t" + header.number.size); break;
    case INTEGER_FORMAT: out.println(String.valueOf(header.linteger.size)); break;
    case FLOAT_FORMAT: out.println(String.valueOf(header.lfloat.size)); break;
    default: throw new IllegalStateException();
    }
  }
  
  public void disassemble(Output out, BHeader chunk, LFunction function) {
    out.print(this.token + "\t");
    switch(this) {
    case SOURCE: out.println(StringUtils.toPrintString(function.name.deref())); break;
    case LINEDEFINED: out.println(String.valueOf(function.linedefined)); break;
    case LASTLINEDEFINED: out.println(String.valueOf(function.lastlinedefined)); break;
    case NUMPARAMS: out.println(String.valueOf(function.numParams)); break;
    case IS_VARARG: out.println(String.valueOf(function.vararg)); break;
    case MAXSTACKSIZE: out.println(String.valueOf(function.maximumStackSize)); break;
    default: throw new IllegalStateException();
    }
  }
  
}
