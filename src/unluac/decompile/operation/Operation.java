package unluac.decompile.operation;

import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.statement.Statement;

abstract public class Operation {

  public final int line;
  
  public Operation(int line) {
    this.line = line;
  }
  
  abstract public List<Statement> process(Registers r, Block block);
  
}
