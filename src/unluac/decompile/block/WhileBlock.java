package unluac.decompile.block;

import unluac.Version;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.condition.Condition;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class WhileBlock extends ContainerBlock {

  private Condition cond;
  private final int unprotectedTarget;
  private final boolean splitable;
  
  private Expression condexpr;
  
  public WhileBlock(LFunction function, Condition cond, int begin, int end, int unprotectedTarget) {
    super(function, begin, end, -1);
    this.cond = cond;
    this.unprotectedTarget = unprotectedTarget;
    this.splitable = function.header.version != Version.LUA50;
  }
  
  @Override
  public void resolve(Registers r) {
    condexpr = cond.asExpression(r);
  }
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    condexpr.walk(w);
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
  public boolean isUnprotected() {
    return unprotectedTarget != -1;
  }
  
  @Override
  public int getUnprotectedLine() {
    if(unprotectedTarget == -1) {
      throw new IllegalStateException();
    }
    return end - 1;
  }
  
  @Override
  public int getUnprotectedTarget() {
    if(unprotectedTarget == -1) {
      throw new IllegalStateException();
    }
    return unprotectedTarget;
  };
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public boolean isSplitable() {
    return splitable && cond.isSplitable();
  }
  
  @Override
  public Block[] split(int line) {
    Condition[] conds = cond.split();
    cond = conds[0];
    return new Block[] {
      new IfThenElseBlock(function, conds[1], begin, line + 1, end - 1),
      new ElseEndBlock(function, line + 1, end - 1),
    };
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print("while ");
    condexpr.print(d, out);
    out.print(" do");
    out.println();
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }
  
}
