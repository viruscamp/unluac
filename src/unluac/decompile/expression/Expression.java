package unluac.decompile.expression;

import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;
import unluac.decompile.target.Target;

abstract public class Expression {

  public static final int PRECEDENCE_OR = 1;
  public static final int PRECEDENCE_AND = 2;
  public static final int PRECEDENCE_COMPARE = 3;
  public static final int PRECEDENCE_BOR = 4;
  public static final int PRECEDENCE_BXOR = 5;
  public static final int PRECEDENCE_BAND = 6;
  public static final int PRECEDENCE_SHIFT = 7;
  public static final int PRECEDENCE_CONCAT = 8;
  public static final int PRECEDENCE_ADD = 9;
  public static final int PRECEDENCE_MUL = 10;
  public static final int PRECEDENCE_UNARY = 11;
  public static final int PRECEDENCE_POW = 12;
  public static final int PRECEDENCE_ATOMIC = 13;
  
  public static final int ASSOCIATIVITY_NONE = 0;
  public static final int ASSOCIATIVITY_LEFT = 1;
  public static final int ASSOCIATIVITY_RIGHT = 2;
  
  static public enum BinaryOperation {
    CONCAT("..", PRECEDENCE_CONCAT, ASSOCIATIVITY_RIGHT),
    ADD("+", PRECEDENCE_ADD, ASSOCIATIVITY_LEFT),
    SUB("-", PRECEDENCE_ADD, ASSOCIATIVITY_LEFT),
    MUL("*", PRECEDENCE_MUL, ASSOCIATIVITY_LEFT),
    DIV("/", PRECEDENCE_MUL, ASSOCIATIVITY_LEFT),
    IDIV("//", PRECEDENCE_MUL, ASSOCIATIVITY_LEFT),
    MOD("%", PRECEDENCE_MUL, ASSOCIATIVITY_LEFT),
    POW("^", PRECEDENCE_POW, ASSOCIATIVITY_RIGHT),
    BAND("&", PRECEDENCE_BAND, ASSOCIATIVITY_LEFT),
    BOR("|", PRECEDENCE_BOR, ASSOCIATIVITY_LEFT),
    BXOR("~", PRECEDENCE_BXOR, ASSOCIATIVITY_LEFT),
    SHL("<<", PRECEDENCE_SHIFT, ASSOCIATIVITY_LEFT),
    SHR(">>", PRECEDENCE_SHIFT, ASSOCIATIVITY_LEFT),
    OR("or", PRECEDENCE_OR, ASSOCIATIVITY_NONE),
    AND("and", PRECEDENCE_AND, ASSOCIATIVITY_NONE),
    ;
    
    public final String op;
    public final int precedence;
    public final int associativity;
    
    private BinaryOperation(String op, int precedence, int associativity) {
      this.op = op;
      this.precedence = precedence;
      this.associativity = associativity;
    }
  }
  
  static public enum UnaryOperation {
    UNM("-"),
    NOT("not "),
    LEN("#"),
    BNOT("~"),
    ;
    
    public final String op;
    
    private UnaryOperation(String op) {
      this.op = op;
    }
  }
  
  public static BinaryExpression make(BinaryOperation op, Expression left, Expression right) {
    return make(op, left, right, false);
  }
  
  public static BinaryExpression make(BinaryOperation op, Expression left, Expression right, boolean flip) {
    if(flip) {
      Expression swap = left;
      left = right;
      right = swap;
    }
    return new BinaryExpression(op.op, left, right, op.precedence, op.associativity);
  }
  
  public static UnaryExpression make(UnaryOperation op, Expression expression) {
    return new UnaryExpression(op.op, expression, PRECEDENCE_UNARY);
  }
  
  /**
   * Prints out a sequences of expressions with commas, and optionally
   * handling multiple expressions and return value adjustment.
   */
  public static void printSequence(Decompiler d, Output out, List<Expression> exprs, boolean linebreak, boolean multiple) {
    int n = exprs.size();
    int i = 1;
    for(Expression expr : exprs) {
      boolean last = (i == n);
      if(expr.isMultiple()) {
        last = true;
      }
      if(last) {
        if(multiple) {
          expr.printMultiple(d, out);
        } else {
          expr.print(d, out);
        }
        break;
      } else {
        expr.print(d, out);
        out.print(",");
        if(linebreak) {
          out.println();
        } else {
          out.print(" ");
        }
      }
      i++;
    }
  }
  
  public final int precedence;
  
  public Expression(int precedence) {
    this.precedence = precedence;
  }
  
  protected static void printUnary(Decompiler d, Output out, String op, Expression expression) {
    out.print(op);
    expression.print(d, out);
  }
  
  protected static void printBinary(Decompiler d, Output out, String op, Expression left, Expression right) {
    left.print(d, out);
    out.print(" ");
    out.print(op);
    out.print(" ");
    right.print(d, out);
  }
  
  abstract public void walk(Walker w);
  
  abstract public void print(Decompiler d, Output out);
  
  /**
   * Prints the expression in a context where it is surrounded by braces.
   * (Thus if the expression would begin with a brace, it must be enclosed
   * in parentheses to avoid ambiguity.)
   */
  public void printBraced(Decompiler d, Output out) {
    print(d, out);
  }
  
  /**
   * Prints the expression in a context that accepts multiple values.
   * (Thus, if an expression that normally could return multiple values
   * doesn't, it should use parens to adjust to 1.)
   */
  public void printMultiple(Decompiler d, Output out) {
    print(d, out);
  }
  
  /**
   * Determines the index of the last-declared constant in this expression.
   * If there is no constant in the expression, return -1.
   */
  abstract public int getConstantIndex();
  
  public int getConstantLine() {
    return -1;
  }
  
  public boolean beginsWithParen() {
    return false;
  }
  
  public boolean isNil() {
    return false;
  }
  
  public boolean isClosure() {
    return false;
  }
  
  public boolean isConstant() {
    return false;
  }
  
  /**
   * An ungrouped expression is one that needs to be enclosed in parentheses
   * before it can be dereferenced. This doesn't apply to multiply-valued expressions
   * as those will be given parentheses automatically when converted to a single value.
   * e.g.
   *  (a+b).c; ("asdf"):gsub()
   */
  public boolean isUngrouped() {
    return false;
  }
  
  // Only supported for closures
  public boolean isUpvalueOf(int register) {
    throw new IllegalStateException();
  }
  
  public boolean isBoolean() {
    return false;
  }
  
  public boolean isInteger() {
    return false;
  }
  
  public int asInteger() {
    throw new IllegalStateException();
  }
  
  public boolean isString() {
    return false;
  }
  
  public boolean isIdentifier() {
    return false;
  }
  
  /**
   * Determines if this can be part of a function name.
   * Is it of the form: {Name . } Name
   */
  public boolean isDotChain() {
    return false;
  }
  
  public int closureUpvalueLine() {
    throw new IllegalStateException();
  }
  
  public void printClosure(Decompiler d, Output out, Target name) {
    throw new IllegalStateException();
  }
  
  public String asName() {
    throw new IllegalStateException();
  }
  
  public boolean isTableLiteral() {
    return false;
  }
  
  public boolean isNewEntryAllowed() {
    throw new IllegalStateException();
  }
  
  public void addEntry(TableLiteral.Entry entry) {
    throw new IllegalStateException();
  }
  
  /**
   * Whether the expression has more than one return stored into registers.
   */
  public boolean isMultiple() {
    return false;
  }
  
  public boolean isMemberAccess() {
    return false;
  }
  
  public Expression getTable() {
    throw new IllegalStateException();
  }
  
  public String getField() {
    throw new IllegalStateException();
  }  
  
  public boolean isBrief() {
    return false;
  }
  
  public boolean isEnvironmentTable(Decompiler d) {
    return false;
  }
  
}
