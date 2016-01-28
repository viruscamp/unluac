package unluac.decompile.statement;

import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Function;
import unluac.decompile.Output;
import unluac.decompile.Walker;

abstract public class Statement {

  /**
   * Prints out a sequences of statements on separate lines. Correctly
   * informs the last statement that it is last in a block.
   */
  public static void printSequence(Decompiler d, Output out, List<Statement> stmts) {
    int n = stmts.size();
    for(int i = 0; i < n; i++) {
      boolean last = (i + 1 == n);
      Statement stmt = stmts.get(i);
      if(stmt.beginsWithParen() && (i > 0 || d.getVersion().isAllowedPreceedingSemicolon())) {
        out.print(";");
      }
      if(last) {
        stmt.printTail(d, out);
      } else {
        stmt.print(d, out);
      }
      if(!stmt.suppressNewline()) {
        out.println();
      }
    }
  }
    
  abstract public void print(Decompiler d, Output out);
  
  public void printTail(Decompiler d, Output out) {
    print(d, out);
  }
  
  public String comment;
  
  public void addComment(String comment) {
    this.comment = comment;
  }
  
  public abstract void walk(Walker w);
  
  public boolean beginsWithParen() {
    return false;
  }
  
  public boolean suppressNewline() {
    return false;
  }
  
  public boolean useConstant(Function f, int index) {
    return false;
  }
  
}
