package unluac.decompile.condition;

import unluac.decompile.Constant;
import unluac.decompile.Registers;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;
import unluac.parse.LBoolean;

public class ConstantCondition implements Condition {

  private int register;
  private boolean value;
  
  public ConstantCondition(int register, boolean value) {
    this.register = register;
    this.value = value;
  }
  
  @Override
  public Condition inverse() {
    return new ConstantCondition(register, !value);
  }

  @Override
  public boolean invertible() {
    return true;
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
  public boolean isSplitable() {
    return false;
  }
  
  @Override
  public Condition[] split() {
    throw new IllegalStateException();
  }
  
  @Override
  public Expression asExpression(Registers r) {
    return new ConstantExpression(new Constant(value ? LBoolean.LTRUE : LBoolean.LFALSE), -1);
  }

}
