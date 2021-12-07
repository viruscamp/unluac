package unluac.assemble;

import java.io.IOException;
import java.io.InputStream;

public class Tokenizer {

  private StringBuilder b;
  private InputStream in;
  
  public Tokenizer(InputStream in) {
    this.in = in;
    b = new StringBuilder();
  }
  
  public String next() throws IOException {
    b.setLength(0);
    
    boolean inToken = false;
    boolean inString = false;
    boolean isLPrefix = false;
    boolean inEscape = false;
    
    for(;;) {
      int code = in.read();
      if(code == -1) break;
      char c = (char)code;
      //if(c == '\n') System.out.println("line"); 
      if(inString) {
        if(c == '\\' && !inEscape) {
          inEscape = true;
          b.append(c);
        } else if(c == '"' && !inEscape) {
          b.append(c);
          break;
        } else {
          inEscape = false;
          b.append(c);
        }
      } else if(Character.isWhitespace(c)) {
        if(inToken) {
          break;
        }
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
