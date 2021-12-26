package unluac.decompile.block;

import unluac.Version;
import unluac.decompile.CloseType;
import unluac.decompile.Registers;
import unluac.parse.LFunction;

public class ForBlock51 extends ForBlock {

  public ForBlock51(LFunction function, int begin, int end, int register, CloseType closeType, int closeLine, boolean forvarClose) {
    super(function, begin, end, register, closeType, closeLine, forvarClose);
  }

  @Override
  public void resolve(Registers r) {
    target = r.getTarget(register + 3, begin - 1);
    start = r.getValue(register, begin - 1);
    stop = r.getValue(register + 1, begin - 1);
    step = r.getValue(register + 2, begin - 1);
  }
  
  @Override
  public void handleVariableDeclarations(Registers r) {
    r.setInternalLoopVariable(register, begin - 2, end - 1);
    r.setInternalLoopVariable(register + 1, begin - 2, end - 1);
    r.setInternalLoopVariable(register + 2, begin - 2, end - 1);
    int explicitEnd = end - 2;
    if(forvarClose && r.getVersion().closesemantics.get() != Version.CloseSemantics.LUA54) explicitEnd--;
    r.setExplicitLoopVariable(register + 3, begin - 1, explicitEnd);
  }
  
}
