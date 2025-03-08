package unluac.decompile;

import unluac.Version;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.GlobalExpression;
import unluac.parse.LFunction;

public class Function {

  private Version version;
  private String name;
  private Function parent;
  public final Code code;
  public final Constant[] constants;
  private final CodeExtract extract;
  
  public Function(Function parent, int index, LFunction function) {
    name = parent == null ? "main" : String.valueOf(index);
    this.parent = parent;
    version = function.header.version;
    code = new Code(function);
    constants = new Constant[function.constants.length];
    for(int i = 0; i < constants.length; i++) {
      constants[i] = new Constant(function.constants[i]);
    }
    extract = function.header.extractor;
  }
  
  public String disassemblerName() {
    return name;
  }
  
  public String fullDisassemblerName() {
    return parent == null ? name : parent.fullDisassemblerName() + "/" + name;
  }
  
  public boolean isConstant(int register) {
    return extract.is_k(register);
  }

  public int constantIndex(int register) {
    return extract.get_k(register);
  }

  public ConstantExpression getGlobalName(int constantIndex) {
    Constant constant = constants[constantIndex];
    if(!constant.isIdentifierPermissive(version)) throw new IllegalStateException();
    return new ConstantExpression(constant, true, constantIndex);
  }
  
  public ConstantExpression getConstantExpression(int constantIndex) {
    Constant constant = constants[constantIndex];
    return new ConstantExpression(constant, constant.isIdentifier(version), constantIndex);
  }
  
  public GlobalExpression getGlobalExpression(int constantIndex) {
    return new GlobalExpression(getGlobalName(constantIndex), constantIndex);
  }
  
  public Version getVersion() {
    return version;
  }
  
}
