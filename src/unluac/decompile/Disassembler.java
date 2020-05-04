package unluac.decompile;

import unluac.assemble.Directive;
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
      
      for(Directive directive : function.header.lheader_type.get_directives()) {
        directive.disassemble(out, function.header, function.header.lheader);
      }
      out.println();
    }
    
    String fullname;
    if(parent == null) {
      fullname = name;
    } else {
      fullname = parent + "/" + name;
    }
    out.println(".function\t" + fullname);
    out.println();
    
    for(Directive directive : function.header.function.get_directives()) {
      directive.disassemble(out, function.header, function);
    }
    out.println();
    
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
        out.print(".line\t" + function.lines.get(line - 1).asInt() + "\t");
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
