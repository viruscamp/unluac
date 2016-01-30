package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.Target;
import unluac.parse.LFunction;

abstract public class TForBlock extends ContainerBlock {

  protected final int register;
  protected final int length;
  protected final boolean forvarClose;
  protected final boolean innerClose;
  
  private Target[] targets;
  private Expression[] values;
  
  public TForBlock(LFunction function, int begin, int end, int register, int length, boolean forvarClose, boolean innerClose) {
    super(function, begin, end, -1);
    this.register = register;
    this.length = length;
    this.forvarClose = forvarClose;
    this.innerClose = innerClose;
  }

  abstract protected List<Target> getTargets(Registers r);
  
  abstract protected int getInternalLoopVariableBeginOffset();
  
  abstract protected int getExplicitLoopVariableEndOffset();
  
  public void handleVariableDeclarations(Registers r) {
    int internalBegin = begin + getInternalLoopVariableBeginOffset();
    r.setInternalLoopVariable(register, internalBegin, end - 1);
    r.setInternalLoopVariable(register + 1, internalBegin, end - 1);
    r.setInternalLoopVariable(register + 2, internalBegin, end - 1);
    int explicitEnd = end + getExplicitLoopVariableEndOffset();
    if(forvarClose) explicitEnd--;
    for(int index = 1; index <= length; index++) {
      r.setExplicitLoopVariable(register + 2 + index, begin - 1, explicitEnd);
    }
  }
  
  @Override
  public void resolve(Registers r) {
    List<Target> targets = getTargets(r);
    ArrayList<Expression> values = new ArrayList<Expression>(3);
    Expression value;
    value = r.getValue(register, begin - 1);
    values.add(value);
    if(!value.isMultiple()) {
      value = r.getValue(register + 1, begin - 1);
      values.add(value);
      if(!value.isMultiple()) {
        values.add(r.getValue(register + 2, begin - 1));
      }
    }
    
    this.targets = targets.toArray(new Target[targets.size()]);
    this.values = values.toArray(new Expression[values.size()]);
  }
  
  @Override
  public void walk(Walker w) {
    w.visitStatement(this);
    for(Expression expression : values) {
      expression.walk(w);
    }
    for(Statement statement : statements) {
      statement.walk(w);
    }
  }
  
  @Override
  public int scopeEnd() {
    int scopeEnd = end - 3;
    if(forvarClose) scopeEnd--;
    if(innerClose) scopeEnd--;
    return scopeEnd;
  }
  
  @Override
  public boolean breakable() {
    return true;
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
    out.print("for ");
    targets[0].print(d, out);
    for(int i = 1; i < targets.length; i++) {
      out.print(", ");
      targets[i].print(d, out);
    }
    out.print(" in ");
    values[0].print(d, out);
    for(int i = 1; i < values.length; i++) {
      out.print(", ");
      values[i].print(d, out);
    }
    out.print(" do");
    out.println();
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }

}
