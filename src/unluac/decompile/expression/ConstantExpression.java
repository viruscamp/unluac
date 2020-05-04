package unluac.decompile.expression;

import unluac.decompile.Constant;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;
import unluac.parse.LNil;

public class ConstantExpression extends Expression {

  private final Constant constant;
  private final int index;
  private final int line;
  
  public static ConstantExpression createNil(int line) {
    return new ConstantExpression(new Constant(LNil.NIL), -1, line);
  }
  
  public static ConstantExpression createInteger(int i) {
    return new ConstantExpression(new Constant(i), -1);
  }
  
  public static ConstantExpression createDouble(double x) {
    return new ConstantExpression(new Constant(x), -1);
  }
  
  private static int getPrecedence(Constant constant) {
    if(constant.isNumber() && constant.isNegative()) {
      return PRECEDENCE_UNARY;
    } else {
      return PRECEDENCE_ATOMIC;
    }
  }
  
  public ConstantExpression(Constant constant, int index) {
    this(constant, index, -1);
  }
  
  private ConstantExpression(Constant constant, int index, int line) {
    super(getPrecedence(constant));
    this.constant = constant;
    this.index = index;
    this.line = line;
  }

  @Override
  public void walk(Walker w) {
    w.visitExpression(this);
  }
  
  @Override
  public int getConstantIndex() {
    return index;
  }
  
  @Override
  public int getConstantLine() {
    return line;
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    constant.print(d, out, false);
  }
  
  @Override
  public void printBraced(Decompiler d, Output out) {
    constant.print(d, out, true);
  }
  
  @Override
  public boolean isConstant() {
    return true;
  }
  
  @Override
  public boolean isUngrouped() {
    return true;
  }
  
  @Override
  public boolean isNil() {
    return constant.isNil();
  }
  
  @Override
  public boolean isBoolean() {
    return constant.isBoolean();
  }
  
  @Override
  public boolean isInteger() {
    return constant.isInteger();
  }
  
  @Override
  public int asInteger() {
    return constant.asInteger();
  }
  
  @Override
  public boolean isString() {
    return constant.isString();
  }
  
  @Override
  public boolean isIdentifier() {
    return constant.isIdentifier();
  }
    
  @Override
  public String asName() {
    return constant.asName();
  }
  
  @Override
  public boolean isBrief() {
    return !constant.isString() || constant.asName().length() <= 10;
  }
  
}
