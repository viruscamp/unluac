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
    // TODO Auto-generated method stub
    return this == o;
  }
  
}
