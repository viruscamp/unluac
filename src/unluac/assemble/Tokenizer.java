package unluac.assemble;

import java.io.IOException;
import java.io.Reader;

public class Tokenizer {

  private StringBuilder b;
  private Reader r;
  
  public Tokenizer(Reader r) {
    this.r = r;
    b = new StringBuilder();
  }
  
  public String next() throws IOException {
    b.setLength(0);
    
    boolean inToken = false;
    boolean inString = false;
    
    for(;;) {
      int code = r.read();
      if(code == -1) break;
      char c = (char)code;
      if(Character.isWhitespace(c)) {
        if(inToken && !inString) {
          break;
        } else if(inString) {
          b.append(c);
        }
      } else {
        if(!inToken && c == '"') {
          inString = true;
        }
        inToken = true;
        b.append(c);
      }
    }
    
    if(b.length() == 0) {
      return null;
    } else {
      return b.toString();
    }
  }
  
}
