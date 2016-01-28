package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.condition.Condition;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class RepeatBlock extends Block {

  private final Condition cond;
  private final List<Statement> statements;
  
  private Expression condexpr;
  
  public RepeatBlock(LFunction function, Condition cond, int begin, int end) {
    super(function, begin, end);
    this.cond = cond;
    statements = new ArrayList<Statement>(end - begin + 1);
  }
  
  @Override
  public void resolve(Registers r) {
    condexpr = cond.asExpression(r);
  }
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    for(Statement statement : statements) {
      w.visitStatement(statement);
    }
    w.visitExpression(condexpr);
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
    return false;
  }
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print("repeat");
    out.println();
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("until ");
    condexpr.print(d, out);
  }
  
}
