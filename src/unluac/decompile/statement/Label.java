package unluac.decompile.statement;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;

public class Label extends Statement {

  private String name;
  
  public Label(int line) {
    name = "lbl_" + line;
  }
  
  public void walk(Walker w) {
    w.visitStatement(this);
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print("::" + name + "::");
  }

}
