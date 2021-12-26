package unluac.decompile.block;

import unluac.decompile.CloseType;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.statement.Return;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class OuterBlock extends ContainerBlock {

  public OuterBlock(LFunction function, int length) {
    super(function, 0, length + 1, CloseType.NONE, -1, -2);
  }
  
  @Override
  public boolean breakable() {
    return false;
  }
  
  @Override
  public boolean isUnprotected() {
    return false;
  }
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public int scopeEnd() {
    return (end - 1) + function.header.version.outerblockscopeadjustment.get();
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    /* extra return statement */
    int last = statements.size() - 1;
    if(last < 0 || !(statements.get(last) instanceof Return)) {
      throw new IllegalStateException(statements.get(last).toString());
    }
    statements.remove(last);
    Statement.printSequence(d, out, statements);
  }
  
}
