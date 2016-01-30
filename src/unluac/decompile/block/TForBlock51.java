package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.target.Target;
import unluac.parse.LFunction;

public class TForBlock51 extends TForBlock {

  public TForBlock51(LFunction function, int begin, int end, int register, int length, boolean forvarClose, boolean innerClose) {
    super(function, begin, end, register, length, forvarClose, innerClose);
  }
  
  @Override
  public List<Target> getTargets(Registers r) {
    ArrayList<Target> targets = new ArrayList<Target>();
    targets.add(r.getTarget(register + 3, begin - 1));
    for(int r1 = register + 4; r1 <= register + 2 + length; r1++) {
      targets.add(r.getTarget(r1, begin - 1));
    }
    return targets;
  }
  
  @Override
  protected int getInternalLoopVariableBeginOffset() {
    return -2;
  }
  
  @Override
  protected int getExplicitLoopVariableEndOffset() {
    return -3;
  }
  
}
