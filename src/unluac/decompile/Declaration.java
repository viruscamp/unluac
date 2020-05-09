package unluac.decompile;

import unluac.parse.LLocal;

public class Declaration {

  public final String name;
  public final int begin;
  public final int end;
  public int register;
  public boolean tbc;
  
  /**
   * Whether this is an invisible for-loop book-keeping variable.
   */
  public boolean forLoop = false;
  
  /**
   * Whether this is an explicit for-loop declared variable.
   */
  public boolean forLoopExplicit = false;
  
  public Declaration(LLocal local, Code code) {
    int adjust = 0;
    if(local.start >= 1) {
      Op op = code.op(local.start);
      if(op == Op.MMBIN || op == Op.MMBINI || op == Op.MMBINK || op == Op.EXTRAARG) {
        adjust--;
      }
    }
    this.name = local.toString();
    this.begin = local.start + adjust;
    this.end = local.end;
    this.tbc = false;
  }
  
  public Declaration(String name, int begin, int end) {
    this.name = name;
    this.begin = begin;
    this.end = end;
  }
  
  public boolean isSplitBy(int begin, int end) {
    return (begin <= this.begin && this.begin < end && end <= this.end)
        || (begin <= this.end && this.end < end - 1 && this.begin < begin);
  }
  
}
