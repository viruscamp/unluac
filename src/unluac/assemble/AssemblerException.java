package unluac.assemble;

@SuppressWarnings("serial")
public class AssemblerException extends Exception {
  
  AssemblerException(int line, String msg) {
    super(String.format("line %d: %s", line, msg));
  }
  
}
