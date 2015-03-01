package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.condition.Condition;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class NewWhileBlock extends Block {

  private final Registers r;
  private final Condition cond;
  private final List<Statement> statements;
  
  public NewWhileBlock(LFunction function, Registers r, Condition cond, int begin, int end) {
    super(function, begin, end);
    this.r = r;
    this.cond = cond;
    statements = new ArrayList<Statement>(end - begin + 1);
  }
  
  @Override
  public int scopeEnd() {
    return end - 2;
  }
  
  @Override
  public boolean breakable() {
    return true;
  }
  
  @Override
  public boolean isContainer() {
    return true;
  }
  
  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  @Override
  public boolean isUnprotected() {
    return true;
  }
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print("while ");
    cond.asExpression(r).print(d, out);
    out.print(" do");
    out.println();
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }
  
}