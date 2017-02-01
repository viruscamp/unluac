package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.Expression;
import unluac.decompile.expression.UnaryExpression;

public class NotCondition implements Condition {
  
  private Condition cond;
  
  public NotCondition(Condition cond) {
    this.cond = cond;
  }

  @Override
  public Condition inverse() {
    return cond;
  }

  @Override
  public boolean invertible() {
    return true;
  }
  
  @Override
  public int register() {
    return cond.register();
  }
  
  @Override
  public boolean isRegisterTest() {
    return cond.isRegisterTest();
  }
  
  @Override
  public boolean isOrCondition() {
    return false;
  }
  
  @Override
  public Expression asExpression(Registers r) {
    return new UnaryExpression("not ", cond.asExpression(r), Expression.PRECEDENCE_UNARY);
  }
  
  @Override
  public String toString() {
    return "not (" + cond + ")";
  }
  
}
