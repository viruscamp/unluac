package unluac;

public class Configuration {

  public enum Mode {
    DECOMPILE,
    DISASSEMBLE,
    ASSEMBLE,
  }
  
  public enum VariableMode {
    DEFAULT,
    FINDER,
  }
  
  public boolean rawstring = false;
  public Mode mode = Mode.DECOMPILE;
  public VariableMode variable = VariableMode.DEFAULT;
  
}
