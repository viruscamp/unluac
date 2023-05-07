package unluac.decompile.block;

import unluac.decompile.CloseType;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class OnceLoop extends ContainerBlock {
  
  public OnceLoop(LFunction function, int begin, int end) {
    super(function, begin, end, CloseType.NONE, -1, 0);
  }
  
  @Override
  public int scopeEnd() {
    return end - 1;
  }
  
  @Override
  public boolean breakable() {
    return true;
  }
  
  @Override
  public boolean hasHeader() {
    return false;
  }
  
  @Override
  public boolean isUnprotected() {
    return false;
  }
  
  @Override
  public int getLoopback() {
    return begin;
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.println("repeat");
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("until true");
  }

}
