package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.Version;
import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.Target;
import unluac.parse.LFunction;

public class TForBlock extends Block {

  private final int register;
  private final int length;
  private final List<Statement> statements;
  
  private Target[] targets;
  private Expression[] values;
  
  public TForBlock(LFunction function, int begin, int end, int register, int length) {
    super(function, begin, end);
    this.register = register;
    this.length = length;
    statements = new ArrayList<Statement>(end - begin + 1);
  }

  @Override
  public void resolve(Registers r) {
    ArrayList<Target> targets = new ArrayList<Target>();
    if(function.header.version == Version.LUA50) {
      targets.add(r.getTarget(register + 2, begin - 1));
      for(int r1 = register + 3; r1 <= register + 2 + length; r1++) {
        targets.add(r.getTarget(r1, begin - 1));
      }
    } else {
      targets.add(r.getTarget(register + 3, begin - 1));
      for(int r1 = register + 4; r1 <= register + 2 + length; r1++) {
        targets.add(r.getTarget(r1, begin - 1));
      }
    }
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
      w.visitExpression(expression);
    }
    for(Statement statement : statements) {
      w.visitStatement(statement);
    }
  }
  
  @Override
  public int scopeEnd() {
    return end - 3;
  }
  
  @Override
  public boolean breakable() {
    return true;
  }
  
  @Override
  public boolean isContainer() {
    return true;
  }
  
  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);    
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
