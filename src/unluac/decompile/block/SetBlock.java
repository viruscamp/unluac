package unluac.decompile.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import unluac.parse.LFunction;

public class SetBlock extends Block {
  
  public final int target;
  private Assignment assign;
  public final Condition cond;
  private Registers r;
  private boolean finalize = false;
  
  public SetBlock(LFunction function, Condition cond, int target, int line, int begin, int end, Registers r) {
    super(function, begin, end, 2);
    if(begin == end) throw new IllegalStateException();
    this.target = target;
    this.cond = cond;
    this.r = r;
    if(target == -1) {
      throw new IllegalStateException();
    }
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
      Assignment assignOut = new Assignment(assign.getFirstTarget(), getValue(), assign.getFirstLine());
      assignOut.print(d, out);
    } else {
      throw new IllegalStateException();
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
  public boolean isEmpty() {
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
      assign.replaceValue(target, getValue());
      return new Operation(end - 1) {
        
        @Override
        public List<Statement> process(Registers r, Block block) {
          return Arrays.asList(assign);
        }
        
      };
    } else {
      return new Operation(end - 1) {
        
        @Override
        public List<Statement> process(Registers r, Block block) {
          if(r.isLocal(target, end - 1)) {
            return Arrays.asList(new Assignment(r.getTarget(target, end - 1), cond
                .asExpression(r), end - 1));
          }
          r.setValue(target, end - 1, cond.asExpression(r));
          return Collections.emptyList();
        }
        
      };
      // return super.process();
    }
  }
  
}
