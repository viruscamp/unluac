package unluac.decompile.block;

import unluac.decompile.condition.Condition;
import unluac.parse.LFunction;

public class WhileBlock50 extends WhileBlock {

  private final int enterTarget;
  
  public WhileBlock50(LFunction function, Condition cond, int begin, int end, int enterTarget) {
    super(function, cond, begin, end, -1);
    this.enterTarget = enterTarget;
  }
  
  @Override
  public int scopeEnd() {
    return enterTarget - 1;
  }
  
  @Override
  public boolean isUnprotected() {
    return false;
  }
  
}
