package unluac.decompile;

@SuppressWarnings("serial")
public class DecompilerException extends RuntimeException {

  public DecompilerException(Function f, int line, String message) {
    super(f.fullDisassemblerName() + " " + line + ": " + message);
  }
  
}
