package unluac.decompile;

import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.GlobalExpression;
import unluac.parse.LFunction;

public class Function {

  private Constant[] constants;
  private final int constantsOffset;
  
  public Function(LFunction function) {
    constants = new Constant[function.constants.length];
    for(int i = 0; i < constants.length; i++) {
      constants[i] = new Constant(function.constants[i]);
    }
    constantsOffset = function.header.version.getConstantsOffset();
  }
  
  public boolean isConstant(int register) {
    return register >= constantsOffset;
  }

  public int constantIndex(int register) {
    return register - constantsOffset;
  }

  public ConstantExpression getGlobalName(int constantIndex) {
    if(!constants[constantIndex].isIdentifier()) throw new IllegalStateException();
    return getConstantExpression(constantIndex);
  }
  
  public ConstantExpression getConstantExpression(int constantIndex) {
    return new ConstantExpression(constants[constantIndex], constantIndex);
  }
  
  public GlobalExpression getGlobalExpression(int constantIndex) {
    return new GlobalExpression(getGlobalName(constantIndex), constantIndex);
  }
  
}
