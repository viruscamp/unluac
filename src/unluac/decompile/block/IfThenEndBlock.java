package unluac.decompile.block;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.condition.AndCondition;
import unluac.decompile.condition.Condition;
import unluac.decompile.condition.OrCondition;
import unluac.decompile.condition.SetCondition;
import unluac.decompile.expression.Expression;
import unluac.decompile.operation.Operation;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class IfThenEndBlock extends ContainerBlock {

  private final Condition cond;
  private final Registers r;
  
  private Expression condexpr;
  
  public IfThenEndBlock(LFunction function, Registers r, Condition cond, int begin, int end) {
    super(function, begin, end, -1);
    this.r = r;
    this.cond = cond;
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
  public boolean breakable() {
    return false;
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
  public Operation process(Decompiler d) {
    final int test = cond.register();
    if(!scopeUsed && test >= 0 && r.getUpdated(test, end - 1) >= begin) {
      // Check for a single assignment
      Assignment assign = null;
      if(statements.size() == 1) {
        Statement stmt = statements.get(0);
        if(stmt instanceof Assignment) {
          assign = (Assignment)stmt;
          if(assign.getArity() != 1) {
            assign = null;
          }
        }
      }
      if(assign != null && assign.getFirstTarget().isLocal() && assign.getFirstTarget().getIndex() == test || statements.isEmpty()) {
        Condition finalset = new SetCondition(end - 1, test);
        Condition combined;
        
        if(cond.invertible()) {
          combined = new OrCondition(cond.inverse(), finalset);
        } else {
          combined = new AndCondition(cond, finalset);
        }
        final Assignment fassign;
        if(assign != null) {
          fassign = new Assignment(assign.getFirstTarget(), combined.asExpression(r));
        } else {
          fassign = null;
        }
        final Condition fcombined = combined;
        return new Operation(end - 1) {
          
          @Override
          public Statement process(Registers r, Block block) {
            if(fassign == null) {
              r.setValue(test, end - 1, fcombined.asExpression(r));
            }
            return fassign;
          }
          
        };
      }
    }
    return super.process(d);
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
    out.print("end");
  }
    
}