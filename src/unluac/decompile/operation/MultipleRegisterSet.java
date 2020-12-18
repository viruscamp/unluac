package unluac.decompile.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;

public class MultipleRegisterSet extends Operation {

  public final int registerFirst;
  public final int registerLast;
  public final Expression value;
  
  public MultipleRegisterSet(int line, int registerFirst, int registerLast, Expression value) {
    super(line);
    this.registerFirst = registerFirst;
    this.registerLast = registerLast;
    this.value = value;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    int count = 0;
    Assignment assignment = new Assignment();
    for(int register = registerFirst; register <= registerLast; register++) {
      r.setValue(register, line, value);
      if(r.isAssignable(register, line)) {
        assignment.addLast(r.getTarget(register, line), value, line);
        count++;
      }
    }
    if(count > 0) {
      return Arrays.asList(assignment);
    } else {
      return Collections.emptyList();
    }
  }
}
