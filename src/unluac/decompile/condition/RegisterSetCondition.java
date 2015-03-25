package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.Expression;

public class RegisterSetCondition implements Condition {

  private int line;
  private int register;
  
  public RegisterSetCondition(int line, int register) {
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
  public Expression asExpression(Registers r) {
    return r.getExpression(register, line + 1);
  }
  
  @Override
  public String toString() {
    return "(" + register + ")";
  }
  
}
