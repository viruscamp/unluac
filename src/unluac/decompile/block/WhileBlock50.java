package unluac.decompile.block;

import unluac.decompile.CloseType;
import unluac.decompile.condition.Condition;
import unluac.parse.LFunction;

public class WhileBlock50 extends WhileBlock {

  private final int enterTarget;
  
  public WhileBlock50(LFunction function, Condition cond, int begin, int end, int enterTarget, CloseType closeType, int closeLine) {
    super(function, cond, begin, end, closeType, closeLine);
    this.enterTarget = enterTarget;
  }
  
  @Override
  public int scopeEnd() {
    return usingClose && closeType != CloseType.NONE ? closeLine - 1 : enterTarget - 1;
  }
  
  @Override
  public boolean isUnprotected() {
    return false;
  }
  
}
