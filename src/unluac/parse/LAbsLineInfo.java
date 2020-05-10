package unluac.parse;

public class LAbsLineInfo extends LObject {

  public final int pc;
  public final int line;
  
  public LAbsLineInfo(int pc, int line) {
    this.pc = pc;
    this.line = line;
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LAbsLineInfo) {
      LAbsLineInfo other = (LAbsLineInfo) o;
      return pc == other.pc && line == other.line;
    } else {
      return false;
    }
  }
  
}
