package unluac.decompile.block;

import unluac.decompile.Registers;
import unluac.parse.LFunction;

public class ForBlock51 extends ForBlock {

  public ForBlock51(LFunction function, int begin, int end, int register, boolean forvarClose, boolean innerClose) {
    super(function, begin, end, register, forvarClose, innerClose);
  }

  @Override
  public void handleVariableDeclarations(Registers r) {
    r.setInternalLoopVariable(register, begin - 2, end - 1);
    r.setInternalLoopVariable(register + 1, begin - 2, end - 1);
    r.setInternalLoopVariable(register + 2, begin - 2, end - 1);
    int explicitEnd = end - 2;
    if(forvarClose) explicitEnd--;
    r.setExplicitLoopVariable(register + 3, begin - 1, explicitEnd);
  }
  
}
