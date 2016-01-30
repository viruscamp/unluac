package unluac.decompile.block;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class DoEndBlock extends ContainerBlock {

  public DoEndBlock(LFunction function, int begin, int end) {
    super(function, begin, end, 1);
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
  public void print(Decompiler d, Output out) {
    out.println("do");
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }
  
}
