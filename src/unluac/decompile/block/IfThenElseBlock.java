package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.condition.Condition;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class IfThenElseBlock extends Block {

  private final Condition cond;
  private final Registers r;
  private final List<Statement> statements;
  private final int elseTarget;
  public ElseEndBlock partner;
  
  public IfThenElseBlock(LFunction function, Registers r, Condition cond, int begin, int end, int elseTarget) {
    super(function, begin, end);
    this.r = r;
    this.cond = cond;
    this.elseTarget = elseTarget;
    statements = new ArrayList<Statement>(end - begin + 1);
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
  public boolean isContainer() {
    return true;
  }
  
  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  @Override
  public int scopeEnd() {
    return end - 2;
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
    cond.asExpression(r).print(d, out);
    out.print(" then");
    out.println();
    out.indent();
    //Handle the case where the "then" is empty in if-then-else.
    //The jump over the else block is falsely detected as a break.
    /*
    if(statements.size() == 1 && statements.get(0) instanceof Break) {
      Break b = (Break) statements.get(0);
      if(b.target == loopback) {
        out.dedent();
        return;
      }
    }
    */
    Statement.printSequence(d, out, statements);
    out.dedent();
    
    // Handle the "empty else" case
    if(end == elseTarget) {
      out.println("else");
      out.println("end");
    }
    
  }
  
}
