package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.Expression;

public interface Condition {
  
  public Condition inverse();
  
  public boolean invertible();
  
  public int register();
  
  public boolean isRegisterTest();
  
  public boolean isOrCondition();
  
  public Expression asExpression(Registers r);
  
  @Override
  public String toString();
  
}
