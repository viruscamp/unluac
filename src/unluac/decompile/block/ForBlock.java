package unluac.decompile.block;

import unluac.decompile.CloseType;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.Target;
import unluac.parse.LFunction;

abstract public class ForBlock extends ContainerBlock {

  protected final int register;
  protected final boolean forvarClose;
  
  protected Target target;
  protected Expression start;
  protected Expression stop;
  protected Expression step;
  
  public ForBlock(LFunction function, int begin, int end, int register, CloseType closeType, int closeLine, boolean forvarClose) {
    super(function, begin, end, closeType, closeLine, -1);
    this.register = register;
    this.forvarClose = forvarClose;
  }

  abstract public void handleVariableDeclarations(Registers r);
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    start.walk(w);
    stop.walk(w);
    step.walk(w);
    for(Statement statement : statements) {
      statement.walk(w);
    }
  }
  
  @Override
  public int scopeEnd() {
    int scopeEnd = end - 2;
    if(forvarClose) scopeEnd--;
    if(usingClose && (closeType == CloseType.CLOSE || closeType == CloseType.JMP)) scopeEnd--;
    return scopeEnd;
  }
  
  @Override
  public boolean breakable() {
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
    target.print(d, out, false);
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
