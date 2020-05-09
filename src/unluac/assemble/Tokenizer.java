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
    boolean isLPrefix = false;
    
    for(;;) {
      int code = r.read();
      if(code == -1) break;
      char c = (char)code;
      //if(c == '\n') System.out.println("line"); 
      if(Character.isWhitespace(c)) {
        if(inToken && !inString) {
          break;
        } else if(inString) {
          b.append(c);
        }
      } else if(inString && c == '"') {
        b.append(c);
        break;
      } else {
        if((!inToken || isLPrefix) && c == '"') {
          inString = true;
        } else if(!inToken && c == 'L') {
          isLPrefix = true;
        } else {
          isLPrefix = false;
        }
        inToken = true;
        b.append(c);
      }
    }
    
    //System.out.println("token: <" + b.toString() + ">");
    
    if(b.length() == 0) {
      return null;
    } else {
      return b.toString();
    }
  }
  
}
