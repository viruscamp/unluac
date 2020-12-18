package unluac.decompile.operation;

import java.util.Arrays;
import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.GlobalTarget;

public class GlobalSet extends Operation {

  private ConstantExpression global;
  private Expression value;
  
  public GlobalSet(int line, ConstantExpression global, Expression value) {
    super(line);
    this.global = global;
    this.value = value;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    return Arrays.asList(new Assignment(new GlobalTarget(global), value, line));
  }
  
}
