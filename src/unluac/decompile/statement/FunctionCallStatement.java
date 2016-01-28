package unluac.decompile.statement;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;
import unluac.decompile.expression.FunctionCall;

public class FunctionCallStatement extends Statement {

  private FunctionCall call;
  
  public FunctionCallStatement(FunctionCall call) {
    this.call = call;
  }

  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    call.walk(w);
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    call.print(d, out);
  }
  
  @Override
  public boolean beginsWithParen() {
    return call.beginsWithParen();
  }
  
}
