package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;

public class FinalSetCondition implements Condition {

  public static enum Type {
    NONE,
    REGISTER,
    VALUE,
  }
  
  public int line;
  private int register;
  public Type type;
  
  public FinalSetCondition(int line, int register) {
    this.line = line;
    this.register = register;
    this.type = Type.NONE;
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
  public boolean isSplitable() {
    return false;
  }
  
  @Override
  public Condition[] split() {
    throw new IllegalStateException();
  }
  
  @Override
  public Expression asExpression(Registers r) {
    Expression expr;
    switch(type) {
      case REGISTER:
        expr = r.getExpression(register, line + 1);
        break;
      case VALUE:
        expr = r.getValue(register, line + 1);
        break;
      case NONE:
      default:
        expr = ConstantExpression.createDouble(register + ((double)line) / 100.0);
        break;
    }
    if(expr == null) {
      throw new IllegalStateException();
    }
    return expr;
  }
  
  @Override
  public String toString() {
    return "(" + register + ")";
  }
  
}