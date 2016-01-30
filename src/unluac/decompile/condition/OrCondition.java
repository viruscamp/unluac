package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.BinaryExpression;
import unluac.decompile.expression.Expression;

public class OrCondition implements Condition {
  
  private Condition left;
  private Condition right;
  
  public OrCondition(Condition left, Condition right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Condition inverse() {
    if(invertible()) {
      return new AndCondition(left.inverse(), right.inverse());
    } else {
      return new NotCondition(this);
    }
  }

  @Override
  public boolean invertible() {
    return right.invertible();
  }
  
  @Override
  public int register() {
    return right.register();
  }
  
  @Override
  public Expression asExpression(Registers r) {
    return new BinaryExpression("or", left.asExpression(r), right.asExpression(r), Expression.PRECEDENCE_OR, Expression.ASSOCIATIVITY_NONE);
  }
  
  @Override
  public String toString() {
    return "(" + left + " or " + right + ")";
  }
  
}
