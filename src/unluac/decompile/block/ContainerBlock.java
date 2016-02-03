package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Walker;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

abstract public class ContainerBlock extends Block {

  protected final List<Statement> statements;
  
  public ContainerBlock(LFunction function, int begin, int end, int priority) {
    super(function, begin, end, priority);
    statements = new ArrayList<Statement>(end - begin + 1);
  }
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    for(Statement statement : statements) {
      statement.walk(w);
    }
  }
  
  @Override
  public boolean isContainer() {
    return true;
  }
  
  @Override
  public boolean isEmpty() {
    return statements.isEmpty();
  }
  
  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
}
