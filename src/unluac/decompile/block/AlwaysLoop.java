package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class AlwaysLoop extends Block {
  
  private final List<Statement> statements;
  
  public AlwaysLoop(LFunction function, int begin, int end) {
    super(function, begin, end);
    statements = new ArrayList<Statement>();
  }
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    for(Statement statement : statements) {
      statement.walk(w);
    }
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
  public boolean isUnprotected() {
    return true;
  }
  
  @Override
  public int getUnprotectedTarget() {
    return begin;
  }
  
  @Override
  public int getUnprotectedLine() {
    return end - 1;
  }
  
  @Override
  public int getLoopback() {
    return begin;
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.println("while true do");
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }

  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
}