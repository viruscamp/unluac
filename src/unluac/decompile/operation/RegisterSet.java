package unluac.decompile.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;

public class RegisterSet extends Operation {

  public final int register;
  public final Expression value;
  
  public RegisterSet(int line, int register, Expression value) {
    super(line);
    this.register = register;
    this.value = value;
    /*
    if(value.isMultiple()) {
      System.out.println("-- multiple @" + register);
    }
    */
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    //System.out.println("-- processing register set " + register + "@" + line);
    r.setValue(register, line, value);
    /*
    if(value.isMultiple()) {
      System.out.println("-- process multiple @" + register);
    }
    */
    if(r.isAssignable(register, line)) {
      //System.out.println("-- assignment!");
      return Arrays.asList(new Assignment(r.getTarget(register, line), value, line));
    } else {
      return Collections.emptyList();
    }
  }
  
}
