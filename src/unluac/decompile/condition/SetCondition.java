package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.Expression;

public class SetCondition implements Condition {

  private int line;
  private int register;
  
  public SetCondition(int line, int register) {
    this.line = line;
    this.register = register;
    if(register < 0) {
      throw new IllegalStateException();
    }
  }
  
  @Override
  public Condition inverse() {
    return new NotCondition(this);
  }

  @Override
  public boolean invertible() {
    return false;
  }
  
  @Override
  public int register() {
    return register;
  }

  @Override
  public boolean isRegisterTest() {
    return false;
  }
  
  @Override
  public boolean isOrCondition() {
    return false;
  }
  
  @Override
  public Expression asExpression(Registers r) {
    return r.getValue(register, line + 1);
  }
  
  @Override
  public String toString() {
    return "(" + register + ")";
  }
  
}
