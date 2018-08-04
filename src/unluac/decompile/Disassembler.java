package unluac.decompile;

import unluac.parse.LFunction;

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
      out.println(".version " + function.header.version.getName());
      
      out.println();
    }
    
    String fullname;
    if(parent == null) {
      fullname = name;
    } else {
      fullname = parent + "/" + name;
    }
    out.println(".function " + fullname);
    out.println();
    for(int line = 1; line <= function.code.length; line++) {
      Op op = code.op(line);
      out.println(op.codePointToString(code.codepoint(line), code.getExtractor()));
      //out.println("\t" + code.opcode(line) + " " + code.A(line) + " " + code.B(line) + " " + code.C(line) + " " + code.Bx(line) + " " + code.sBx(line) + " " + code.codepoint(line));
    }
    out.println();
    for(int constant = 1; constant <= function.constants.length; constant++) {
      out.println("\t" + constant + " " + function.constants[constant - 1]);
    }
    out.println();
    int subindex = 0;
    for(LFunction child : function.functions) {
      new Disassembler(child, "f" + subindex, fullname).disassemble(out, level + 1, subindex);
      subindex++;
    }
  }
  
}
