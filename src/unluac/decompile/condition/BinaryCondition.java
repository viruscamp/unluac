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
  private final Operand left;
  private final Operand right;
  private final boolean inverted;
  
  public BinaryCondition(Operator op, int line, Operand left, Operand right) {
    this(op, line, left, right, false);
  }
  
  private BinaryCondition(Operator op, int line, Operand left, Operand right, boolean inverted) {
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
    boolean transpose = false;
    Expression leftExpression = left.asExpression(r, line);
    Expression rightExpression = right.asExpression(r, line);
    if(op != Operator.EQ || right.type == OperandType.K) {
      if(left.isRegister(r) && right.isRegister(r)) {
        transpose = left.getUpdated(r, line) > right.getUpdated(r, line);
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
