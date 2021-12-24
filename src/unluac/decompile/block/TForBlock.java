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

public class TForBlock extends ContainerBlock {

  protected final int internalRegisterFirst;
  protected final int internalRegisterLast;
  
  protected final int explicitRegisterFirst;
  protected final int explicitRegisterLast;
  
  protected final int internalScopeBegin;
  protected final int internalScopeEnd;
  
  protected final int explicitScopeBegin;
  protected final int explicitScopeEnd;
  
  protected final int innerScopeEnd;
  
  private Target[] targets;
  private Expression[] values;
  
  public static TForBlock make50(LFunction function, int begin, int end, int register, int length, boolean innerClose) {
    int innerScopeEnd = end - 3;
    if(innerClose) {
      innerScopeEnd--;
    }
    return new TForBlock(
      function, begin, end,
      register, register + 1, register + 2, register + 1 + length,
      begin - 1, end - 1,
      begin - 1, end - 1,
      innerScopeEnd
    );
  }
  
  public static TForBlock make51(LFunction function, int begin, int end, int register, int length, boolean forvarClose, boolean innerClose) {
    int explicitScopeEnd = end - 3;
    int innerScopeEnd = end - 3;
    if(forvarClose) {
      explicitScopeEnd--;
      innerScopeEnd--;
    }
    if(innerClose) {
      innerScopeEnd--;
    }
    return new TForBlock(
      function, begin, end,
      register, register + 2, register + 3, register + 2 + length,
      begin - 2, end - 1,
      begin - 1, explicitScopeEnd,
      innerScopeEnd
    );
  }
  
  public static TForBlock make54(LFunction function, int begin, int end, int register, int length) {
    return new TForBlock(
      function, begin, end,
      register, register + 3, register + 4, register + 3 + length,
      begin - 2, end,
      begin - 1, end - 3,
      end - 3
    );
  }
  
  public TForBlock(LFunction function, int begin, int end,
    int internalRegisterFirst, int internalRegisterLast,
    int explicitRegisterFirst, int explicitRegisterLast,
    int internalScopeBegin, int internalScopeEnd,
    int explicitScopeBegin, int explicitScopeEnd,
    int innerScopeEnd
  ) {
    super(function, begin, end, -1);
    this.internalRegisterFirst = internalRegisterFirst;
    this.internalRegisterLast = internalRegisterLast;
    this.explicitRegisterFirst = explicitRegisterFirst;
    this.explicitRegisterLast = explicitRegisterLast;
    this.internalScopeBegin = internalScopeBegin;
    this.internalScopeEnd = internalScopeEnd;
    this.explicitScopeBegin = explicitScopeBegin;
    this.explicitScopeEnd = explicitScopeEnd;
    this.innerScopeEnd = innerScopeEnd;
  }

  public List<Target> getTargets(Registers r) {
    ArrayList<Target> targets = new ArrayList<Target>(explicitRegisterLast - explicitRegisterFirst + 1);
    for(int register = explicitRegisterFirst; register <= explicitRegisterLast; register++) {
      targets.add(r.getTarget(register, begin - 1));
    }
    return targets;
  }
  
  public void handleVariableDeclarations(Registers r) {
    for(int register = internalRegisterFirst; register <= internalRegisterLast; register++) {
      r.setInternalLoopVariable(register, internalScopeBegin, internalScopeEnd);
    }
    for(int register = explicitRegisterFirst; register <= explicitRegisterLast; register++) {
      r.setExplicitLoopVariable(register, explicitScopeBegin, explicitScopeEnd);
    }
  }
  
  @Override
  public void resolve(Registers r) {
    List<Target> targets = getTargets(r);
    ArrayList<Expression> values = new ArrayList<Expression>(3);
    for(int register = internalRegisterFirst; register <= internalRegisterLast; register++) {
      Expression value = r.getValue(register, begin - 1);
      values.add(value);
      if(value.isMultiple()) break;
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
    return innerScopeEnd;
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
    targets[0].print(d, out, false);
    for(int i = 1; i < targets.length; i++) {
      out.print(", ");
      targets[i].print(d, out, false);
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
