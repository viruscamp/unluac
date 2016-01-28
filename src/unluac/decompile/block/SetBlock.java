package unluac.decompile.block;

import unluac.decompile.ControlFlowHandler;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.condition.Condition;
import unluac.decompile.expression.Expression;
import unluac.decompile.operation.Operation;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.Target;
import unluac.parse.LFunction;

public class SetBlock extends Block {
  
  public final int target;
  private Assignment assign;
  public final Condition cond;
  private Registers r;
  private boolean finalize = false;
  
  public SetBlock(LFunction function, Condition cond, int target, int line,
      int begin, int end, Registers r) {
    super(function, begin, end);
    if(begin == end)
      this.begin -= 1;
    this.target = target;
    this.cond = cond;
    this.r = r;
    // System.out.println("-- set block " + begin + " .. " + end);
  }
  
  public void walk(Walker w) {
    throw new IllegalStateException();
  }
  
  @Override
  public void addStatement(Statement statement) {
    if(!finalize && statement instanceof Assignment) {
      this.assign = (Assignment) statement;
    }/* else if(statement instanceof BooleanIndicator) {
      finalize = true;
    }*/
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
    if(assign != null && assign.getFirstTarget() != null) {
      Assignment assignOut = new Assignment(assign.getFirstTarget(), getValue());
      assignOut.print(d, out);
    } else {
      out.print("-- unhandled set block");
      out.println();
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
  
  public void useAssignment(Assignment assign) {
    this.assign = assign;
    // branch.useExpression(assign.getFirstValue());
  }
  
  public Expression getValue() {
    return cond.asExpression(r);
  }
  
  @Override
  public Operation process(final Decompiler d) {
    if(ControlFlowHandler.verbose) {
      System.out.print("set expression: ");
      cond.asExpression(r).print(d, new Output());
      System.out.println();
    }
    if(assign != null) {
      // branch.useExpression(assign.getFirstValue());
      final Target target = assign.getFirstTarget();
      final Expression value = getValue();
      return new Operation(end - 1) {
        
        @Override
        public Statement process(Registers r, Block block) {
          // System.out.println(begin + " .. " + end);
          return new Assignment(target, value);
        }
        
      };
    } else {
      return new Operation(end - 1) {
        
        @Override
        public Statement process(Registers r, Block block) {
          if(r.isLocal(target, end - 1)) {
            return new Assignment(r.getTarget(target, end - 1), cond
                .asExpression(r));
          }
          r.setValue(target, end - 1, cond.asExpression(r));
          return null;
        }
        
      };
      // return super.process();
    }
  }
  
}
