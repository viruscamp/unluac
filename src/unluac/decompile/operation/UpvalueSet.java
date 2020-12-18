package unluac.decompile.operation;

import java.util.Arrays;
import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.UpvalueTarget;

public class UpvalueSet extends Operation {

  private UpvalueTarget target;
  private Expression value;
  
  public UpvalueSet(int line, String upvalue, Expression value) {
    super(line);
    target = new UpvalueTarget(upvalue);
    this.value = value;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    return Arrays.asList(new Assignment(target, value, line));
  }
  
}
