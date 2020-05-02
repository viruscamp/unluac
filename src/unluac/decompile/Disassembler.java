package unluac.decompile;

import unluac.Version;
import unluac.parse.LFunction;
import unluac.parse.LLocal;
import unluac.parse.LUpvalue;
import unluac.util.StringUtils;

public class Disassembler {

  private final LFunction function;
  private final Code code;
  private final String name;
  private final String parent;
  
  public Disassembler(LFunction function) {
    this(function, "main", null);
  }
  
  private Disassembler(LFunction function, String name, String parent) {
    this.function = function;
    this.code = new Code(function);
    this.name = name;
    this.parent = parent;
  }
  
  public void disassemble(Output out) {
    disassemble(out, 0, 0);
  }
  
  private void disassemble(Output out, int level, int index) {
    if(parent == null) {
      out.println(".version\t" + function.header.version.getName());
      
      out.println();
      
      if(function.header.version == Version.LUA51) {
        out.println(".format\t" + function.header.lheader.format);
        out.println(".endianness\t" + function.header.lheader.endianness);
        out.println(".int_size\t" + function.header.integer.intSize);
        out.println(".size_t_size\t" + function.header.sizeT.sizeTSize);
        out.println(".instruction_size\t4");
        out.println(".number_format\t" + (function.header.number.integral ? "integer" : "float") + "\t" + function.header.number.size);
        out.println();
      }
    }
    
    String fullname;
    if(parent == null) {
      fullname = name;
    } else {
      fullname = parent + "/" + name;
    }
    out.println(".function\t" + fullname);
    out.println();
    
    if(function.header.version == Version.LUA51) {
      out.println(".source\t" + StringUtils.toPrintString(function.name.deref()) + "");
      out.println(".linedefined\t" + function.linedefined);
      out.println(".lastlinedefined\t" + function.lastlinedefined);
      out.println();
      out.println(".numparams\t" + function.numParams);
      out.println(".is_vararg\t" + function.vararg);
      out.println(".maxstacksize\t" + function.maximumStackSize);
      out.println();
    }
    
    if(function.locals.length > 0) {
      for(int local = 1; local <= function.locals.length; local++) {
        LLocal l = function.locals[local - 1];
        out.println(".local\t" + StringUtils.toPrintString(l.name.deref()) + "\t" + l.start + "\t" + l.end);
      }
      out.println();
    }
    
    if(function.upvalues.length > 0) {
      for(int upvalue = 1; upvalue <= function.upvalues.length; upvalue++) {
        LUpvalue u = function.upvalues[upvalue - 1];
        out.println(".upvalue\t" + StringUtils.toPrintString(u.name) + "\t" + u.idx + "\t" + u.instack);
      }
      out.println();
    }
    
    if(function.constants.length > 0) {
      for(int constant = 1; constant <= function.constants.length; constant++) {
        out.println(".constant\tk" + (constant - 1) + "\t" + function.constants[constant - 1]);
      }
      out.println();
    }
    
    boolean[] label = new boolean[function.code.length];
    for(int line = 1; line <= function.code.length; line++) {
      Op op = code.op(line);
      if(op.hasJump()) {
        int target = code.target(line);
        if(target >= 1 && target <= label.length) {
          label[target - 1] = true;
        }
      }
    }
    
    for(int line = 1; line <= function.code.length; line++) {
      if(label[line - 1]) {
        out.println(".label\t" + "l" + line);
      }
      if(line <= function.lines.length.asInt()) {
        out.println(".line\t" + function.lines.get(line - 1).asInt());
      }
      Op op = code.op(line);
      String cpLabel = null;
      if(op.hasJump()) {
        int target = code.target(line);
        if(target >= 1 && target <= code.length) {
          cpLabel = "l" + target;
        }
      }
      out.println(op.codePointToString(code.codepoint(line), code.getExtractor(), cpLabel));
      //out.println("\t" + code.opcode(line) + " " + code.A(line) + " " + code.B(line) + " " + code.C(line) + " " + code.Bx(line) + " " + code.sBx(line) + " " + code.codepoint(line));
    }
    for(int line = function.code.length + 1; line <= function.lines.length.asInt(); line++) {
      out.println(".line\t" + function.lines.get(line - 1).asInt());
    }
    out.println();
    
    int subindex = 0;
    for(LFunction child : function.functions) {
      new Disassembler(child, "f" + subindex, fullname).disassemble(out, level + 1, subindex);
      subindex++;
    }
  }
  
}
