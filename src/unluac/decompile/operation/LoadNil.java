package unluac.decompile.operation;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;

public class LoadNil extends Operation {

  public final int registerFirst;
  public final int registerLast;
  
  public LoadNil(int line, int registerFirst, int registerLast) {
    super(line);
    this.registerFirst = registerFirst;
    this.registerLast = registerLast;
  }

  @Override
  public Statement process(Registers r, Block block) {
    int count = 0;
    Assignment assignment = new Assignment();
    for(int register = registerFirst; register <= registerLast; register++) {
      r.setValue(register, line, Expression.NIL);
      if(r.isAssignable(register, line)) {
        assignment.addLast(r.getTarget(register, line), Expression.NIL);
        count++;
      }
    }
    if(count > 0) {
      return assignment;
    } else {
      return null;
    }
   }
  
}
