package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.Version;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.Target;
import unluac.parse.LFunction;

public class ForBlock extends Block {

  private final int register;
  private final List<Statement> statements;
  
  private Target target;
  private Expression start;
  private Expression stop;
  private Expression step;
  
  public ForBlock(LFunction function, int begin, int end, int register) {
    super(function, begin, end);
    this.register = register;
    statements = new ArrayList<Statement>(end - begin + 1);
  }

  @Override
  public void resolve(Registers r) {
    if(function.header.version == Version.LUA50) {
      target = r.getTarget(register, begin - 1);
      start = r.getValue(register, begin - 2);
    } else {
      start = r.getValue(register, begin - 1);
      target = r.getTarget(register + 3, begin - 1);
    }
    stop = r.getValue(register + 1, begin - 1);
    step = r.getValue(register + 2, begin - 1);
  }
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    w.visitExpression(start);
    w.visitExpression(stop);
    w.visitExpression(step);
    for(Statement statement : statements) {
      w.visitStatement(statement);
    }
  }
  
  @Override
  public int scopeEnd() {
    return end - 2;
  }
  
  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);    
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
    return false;
  }

  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }

  @Override
  public void print(Decompiler d, Output out) {
    out.print("for ");
    target.print(d, out);
    out.print(" = ");
    start.print(d, out);
    out.print(", ");
    stop.print(d, out);
    if(!step.isInteger() || step.asInteger() != 1) {
      out.print(", ");
      step.print(d, out);
    }
    out.print(" do");
    out.println();
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }
  
}
