package unluac.decompile.condition;

import unluac.decompile.Registers;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;

public interface Condition {
  
  public static enum OperandType {
    R,
    RK,
    K,
    I,
  }
  
  public static class Operand {
    
    public Operand(OperandType type, int value) {
      this.type = type;
      this.value = value;
    }
    
    public Expression asExpression(Registers r, int line) {
      switch(type) {
      case R: return r.getExpression(this.value, line);
      case RK: return r.getKExpression(this.value, line);
      case K: return r.getFunction().getConstantExpression(this.value);
      case I: return ConstantExpression.createInteger(this.value);
      default: throw new IllegalStateException();
      }
    }
    
    public boolean isRegister(Registers r) {
      switch(type) {
      case R: return true;
      case RK: return !r.isKConstant(this.value);
      case K: return false;
      case I: return false;
      default: throw new IllegalStateException();
      }
    }
    
    public int getUpdated(Registers r, int line) {
      switch(type) {
      case R: return r.getUpdated(this.value, line);
      case RK:
        if(r.isKConstant(this.value)) throw new IllegalStateException();
        return r.getUpdated(this.value, line);
      default: throw new IllegalStateException();
      }
    }
    
    public final OperandType type;
    public final int value;
    
  }
  
  public Condition inverse();
  
  public boolean invertible();
  
  public int register();
  
  public boolean isRegisterTest();
  
  public boolean isOrCondition();
  
  public boolean isSplitable();
  
  public Condition[] split();
  
  public Expression asExpression(Registers r);
  
  @Override
  public String toString();
  
}
