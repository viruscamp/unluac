package unluac.decompile.block;

import unluac.Version;
import unluac.decompile.CloseType;
import unluac.decompile.Registers;
import unluac.parse.LFunction;

public class ForBlock51 extends ForBlock {

  protected boolean forvarPostClose;
  
  public ForBlock51(LFunction function, int begin, int end, int register, CloseType closeType, int closeLine, boolean forvarPreClose, boolean forvarPostClose) {
    super(function, begin, end, register, closeType, closeLine, forvarPreClose);
    this.forvarPostClose = forvarPostClose;
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
    int implicitEnd = end - 1;
    if(forvarPostClose) implicitEnd++;
    r.setInternalLoopVariable(register, begin - 2, implicitEnd);
    r.setInternalLoopVariable(register + 1, begin - 2, implicitEnd);
    r.setInternalLoopVariable(register + 2, begin - 2, implicitEnd);
    int explicitEnd = end - 2;
    if(forvarPreClose && r.getVersion().closesemantics.get() == Version.CloseSemantics.DEFAULT) explicitEnd--;
    r.setExplicitLoopVariable(register + 3, begin - 1, explicitEnd);
  }
  
}
