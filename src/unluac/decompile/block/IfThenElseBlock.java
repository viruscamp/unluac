package unluac.decompile.block;

import unluac.decompile.CloseType;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.condition.Condition;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class IfThenElseBlock extends ContainerBlock {

  private final Condition cond;
  private final int elseTarget;
  public ElseEndBlock partner;
  
  private Expression condexpr;
  
  public IfThenElseBlock(LFunction function, Condition cond, int begin, int end, int elseTarget, CloseType closeType, int closeLine) {
    super(function, begin, end, closeType, closeLine, -1);
    this.cond = cond;
    this.elseTarget = elseTarget;
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
  public boolean suppressNewline() {
    return true;
  }
  
  @Override
  public int compareTo(Block block) {
    if(block == partner) {
      return -1;
    } else {
      return super.compareTo(block);
    }
  }  
  
  @Override
  public boolean breakable() {
    return false;
  }
  
  @Override
  public int scopeEnd() {
    return usingClose && closeType == CloseType.CLOSE ? closeLine - 1 : end - 2;
  }
  
  @Override
  public boolean hasHeader() {
    return true;
  }
  
  @Override
  public boolean isUnprotected() {
    return true;
  }
  
  @Override
  public int getUnprotectedLine() {
    return end - 1;
  }
  
  @Override
  public int getUnprotectedTarget() {
    return elseTarget;
  }
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print("if ");
    condexpr.print(d, out);
    out.print(" then");
    out.println();
    out.indent();
    
    Statement.printSequence(d, out, statements);
    
    out.dedent();
    
    // Handle the "empty else" case
    if(end == elseTarget) {
      out.println("else");
      out.println("end");
    }
    
  }
  
}
