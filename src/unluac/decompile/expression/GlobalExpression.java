package unluac.decompile.expression;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;

public class GlobalExpression extends Expression {

  private final ConstantExpression name;
  private final int index;
  
  public GlobalExpression(ConstantExpression name, int index) {
    super(PRECEDENCE_ATOMIC);
    this.name = name;
    this.index = index;
  }
  
  @Override
  public int getConstantIndex() {
    return index;
  }

    @Override
  public boolean isDotChain() {
    return true;
  }

  @Override
  public void print(Decompiler d, Output out) {
    out.print(name.asName());
  }
  
  @Override
  public boolean isBrief() {
    return true;
  }
  
}
