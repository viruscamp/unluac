package unluac;

public class Configuration {

  public enum Mode {
    DECOMPILE,
    DISASSEMBLE,
    ASSEMBLE;
  }
  
  public boolean rawstring = false;
  public Mode mode = Mode.DECOMPILE;
  
}
