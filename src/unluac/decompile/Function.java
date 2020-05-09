package unluac.decompile;

import unluac.Version;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.GlobalExpression;
import unluac.parse.LFunction;

public class Function {

  private Version version;
  private Constant[] constants;
  private final CodeExtract extract;
  
  public Function(LFunction function) {
    version = function.header.version;
    constants = new Constant[function.constants.length];
    for(int i = 0; i < constants.length; i++) {
      constants[i] = new Constant(function.constants[i]);
    }
    extract = function.header.extractor;
  }
  
  public boolean isConstant(int register) {
    return extract.is_k(register);
  }

  public int constantIndex(int register) {
    return extract.get_k(register);
  }

  public ConstantExpression getGlobalName(int constantIndex) {
    if(!constants[constantIndex].isIdentifier(version)) throw new IllegalStateException();
    return getConstantExpression(constantIndex);
  }
  
  public ConstantExpression getConstantExpression(int constantIndex) {
    Constant constant = constants[constantIndex];
    return new ConstantExpression(constant, constant.isIdentifier(version), constantIndex);
  }
  
  public GlobalExpression getGlobalExpression(int constantIndex) {
    return new GlobalExpression(getGlobalName(constantIndex), constantIndex);
  }
  
}
