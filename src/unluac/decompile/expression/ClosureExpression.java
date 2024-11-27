package unluac.decompile.expression;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;
import unluac.decompile.target.TableTarget;
import unluac.decompile.target.Target;
import unluac.decompile.target.VariableTarget;
import unluac.parse.LFunction;
import unluac.parse.LUpvalue;

public class ClosureExpression extends Expression {

  private final LFunction function;
  private int upvalueLine;
  private Decompiler d;
  
  public ClosureExpression(LFunction function, int upvalueLine) {
    super(PRECEDENCE_ATOMIC);
    this.function = function;
    this.upvalueLine = upvalueLine;
  }

  @Override
  public void walk(Walker w) {
    w.visitExpression(this);
  }
  
  public int getConstantIndex() {
    return -1;
  }
  
  @Override
  public boolean isClosure() {
    return true;
  }
  
  @Override
  public boolean isUngrouped() {
    return true;
  }
  
  @Override
  public boolean isUpvalueOf(int register) {
    /*
    if(function.header.version == 0x51) {
      return false; //TODO:
    }
    */
    for(int i = 0; i < function.upvalues.length; i++) {
      LUpvalue upvalue = function.upvalues[i];
      if(upvalue.instack && upvalue.idx == register) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public boolean isNameUnbound(Decompiler outer, String id) {
    for(LUpvalue upvalue : function.upvalues) {
      if(upvalue.name.equals(id)) {
        return true;
      }
    }
    Decompiler d = getDecompiler(outer);
    if(function.header.version.hasGlobalSupport()) {
      for(int line = 1; line <= d.code.length; line++) {
        switch(d.code.op(line)) {
          case GETGLOBAL:
          case SETGLOBAL:
            if(function.constants[d.code.Bx(line)].deref().equals(id)) {
              return true;
            }
            break;
          default: break;
        }
      }
      // TODO: recurse
    }
    return false;
  }
  
  @Override
  public int closureUpvalueLine() {
    return upvalueLine;
  }
  
  @Override
  public void print(Decompiler outer, Output out) {
    Decompiler d = getDecompiler(outer);
    out.print("function");
    printMain(out, d, true);
  }
  
  @Override
  public void printClosure(Decompiler outer, Output out, Target name) {
    Decompiler d = getDecompiler(outer);
    out.print("function ");
    if(function.numParams >= 1 && d.declList[0].name.equals("self") && name instanceof TableTarget) {
      name.printMethod(outer, out);
      printMain(out, d, false);
    } else {
      name.print(outer, out, false);
      printMain(out, d, true);
    }
  }
  
  private void printMain(Output out, Decompiler d, boolean includeFirst) {
    out.print("(");
    int start = includeFirst ? 0 : 1;
    if(function.numParams > start) {
      new VariableTarget(d.declList[start]).print(d, out, false);
      for(int i = start + 1; i < function.numParams; i++) {
        out.print(", ");
        new VariableTarget(d.declList[i]).print(d, out, false);
      }
    }
    if(function.vararg != 0) {
      if(function.numParams > start) {
        out.print(", ...");
      } else {
        out.print("...");
      }
    }
    out.print(")");
    out.println();
    out.indent();
    Decompiler.State result = d.decompile();
    d.print(result, out);
    out.dedent();
    out.print("end");
    //out.println(); //This is an extra space for formatting
  }
  
  private Decompiler getDecompiler(Decompiler outer) {
    if(d == null) {
      d = new Decompiler(function, outer.declList, upvalueLine);
    }
    return d;
  }
  
}
