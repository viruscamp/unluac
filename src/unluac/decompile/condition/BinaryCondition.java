package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.BinaryExpression;
import unluac.decompile.expression.Expression;

public class BinaryCondition implements Condition {
  
  public static enum Operator {
    EQ,
    LT,
    LE
  }
  
  private static String operator_to_string(Operator op, boolean inverted, boolean transposed) {
    switch(op) {
      case EQ: return inverted ? "~=" : "==";
      case LT: return transposed ? ">" : "<";
      case LE: return transposed ? ">=" : "<=";
    }
    throw new IllegalStateException();
  }
  
  private final Operator op;
  private final int line;
  private final int left;
  private final int right;
  private final boolean inverted;
  
  public BinaryCondition(Operator op, int line, int left, int right) {
    this(op, line, left, right, false);
  }
  
  private BinaryCondition(Operator op, int line, int left, int right, boolean inverted) {
    this.op = op;
    this.line = line;
    this.left = left;
    this.right = right;
    this.inverted = inverted;
  }
  
  @Override
  public Condition inverse() {
    if(op == Operator.EQ) {
      return new BinaryCondition(op, line, left, right, !inverted);
    } else {
      return new NotCondition(this);
    }
  }
  
  @Override
  public boolean invertible() {
    return op == Operator.EQ;
  }
  
  @Override
  public int register() {
    return -1;
  }
  
  @Override
  public Expression asExpression(Registers r) {
    boolean transpose = false;
    Expression leftExpression = r.getKExpression(left, line);
    Expression rightExpression = r.getKExpression(right, line);
    if(op != Operator.EQ) {
      if(!r.isKConstant(left) && !r.isKConstant(right)) {
        transpose = r.getUpdated(left, line) > r.getUpdated(right, line);
      } else {
        transpose = rightExpression.getConstantIndex() < leftExpression.getConstantIndex();
      }
    }
    String opstring = operator_to_string(op, inverted, transpose);
    Expression rtn = new BinaryExpression(opstring, !transpose ? leftExpression : rightExpression, !transpose ? rightExpression : leftExpression, Expression.PRECEDENCE_COMPARE, Expression.ASSOCIATIVITY_LEFT);
    /*
    if(inverted) {
      rtn = new UnaryExpression("not ", rtn, Expression.PRECEDENCE_UNARY);
    }
    */
    return rtn;
  }
  
  @Override
  public String toString() {
    return left + " " + operator_to_string(op, inverted, false) + " " + right; 
  }
  
}
