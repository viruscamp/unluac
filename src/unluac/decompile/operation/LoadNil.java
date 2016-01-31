package unluac.decompile.operation;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.ConstantExpression;
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
    Expression nil = ConstantExpression.createNil(line);
    for(int register = registerFirst; register <= registerLast; register++) {
      r.setValue(register, line, nil);
      if(r.isAssignable(register, line)) {
        assignment.addLast(r.getTarget(register, line), nil, line);
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
