package unluac.decompile.expression;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.Walker;
import unluac.parse.LFunction;
import unluac.parse.LUpvalue;

public class TableReference extends Expression {

  private final Registers r;
  private final int line;
  private final Expression table;
  private final Expression index;
  
  public TableReference(Registers r, int line, Expression table, Expression index) {
    super(PRECEDENCE_ATOMIC);
    this.r = r;
    this.line = line;
    this.table = table;
    this.index = index;
  }

  @Override
  public void walk(Walker w) {
    w.visitExpression(this);
    table.walk(w);
    index.walk(w);
  }
  
  @Override
  public int getConstantIndex() {
    return Math.max(table.getConstantIndex(), index.getConstantIndex());
  }
  
  private static boolean isUpvalueOf(LFunction function, String id) {
    for(int i = 0; i < function.upvalues.length; i++) {
      LUpvalue upvalue = function.upvalues[i];
      if(upvalue.name.equals(id)) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    boolean isGlobal = table.isEnvironmentTable(d) && index.isIdentifier();
    if(isGlobal) {
      String name = index.asName();
      if(r.isLocalName(name, line) || isUpvalueOf(d.function, name) || d.boundNames.contains(name)) {
        // _ENV lookup reference is shadowed; need explicit _ENV
        isGlobal = false;
      }
    }
    if(!isGlobal) {
      if(table.isUngrouped()) {
        out.print("(");
        table.print(d, out);
        out.print(")");
      }
      else
      {
        table.print(d, out);
      }
    }
    if(index.isIdentifier()) {
      if(!isGlobal) {
        out.print(".");
      }
      out.print(index.asName());
    } else {
      out.print("[");
      index.printBraced(d, out);
      out.print("]");
    }
  }

  @Override
  public boolean isDotChain() {
    return index.isIdentifier() && table.isDotChain();
  }
  
  @Override
  public boolean isMemberAccess() {
    return index.isIdentifier();
  }
  
  @Override
  public boolean beginsWithParen() {
    return table.isUngrouped() || table.beginsWithParen();
  }
  
  @Override
  public Expression getTable() {
    return table;
  }
  
  @Override
  public String getField() {
    return index.asName();
  }

  
}
