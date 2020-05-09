package unluac.decompile.target;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;

public class GlobalTarget extends Target {

  private final Expression name;
  
  public GlobalTarget(ConstantExpression name) {
    this.name = name;
  }

  @Override
  public void walk(Walker w) {
    name.walk(w);
  }
  
  @Override
  public void print(Decompiler d, Output out, boolean declare) {
    out.print(name.asName());
  }
  
  @Override
  public void printMethod(Decompiler d, Output out) {
    throw new IllegalStateException();
  }
  
}
