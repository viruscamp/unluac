package unluac.assemble;

import java.io.IOException;
import java.io.InputStream;

public class Tokenizer {

  private StringBuilder b;
  private InputStream in;
  private int line;
  private int pos;
  private int tokenline;
  private int tokenpos;
  private char lineending;
  
  public Tokenizer(InputStream in) {
    this.in = in;
    this.line = 1;
    this.pos = 0;
    this.tokenline = 1;
    this.tokenpos = 0;
    this.lineending = '\0';
    b = new StringBuilder();
  }
  
  public String next() throws IOException {
    b.setLength(0);
    
    boolean inToken = false;
    boolean inString = false;
    boolean inComment = false;
    boolean isLPrefix = false;
    boolean inEscape = false;
    
    for(;;) {
      if(!inToken) {
        tokenline = line;
        tokenpos = pos;
      }
      int code = in.read();
      if(code == -1) break;
      pos++;
      char c = (char)code;
      char lastlineending = lineending;
      lineending = '\0';
      if(lastlineending == '\r' && c == '\n') {
        // skip
      } else if(inString) {
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
      } else if(inComment) {
        if(c == '\n' || c == '\r') {
          line++;
          pos = 0;
          lineending = c;
          inComment = false;
          if(inToken) {
            break;
          }
        }
      } else if(c == ';') {
        inComment = true;
      } else if(Character.isWhitespace(c)) {
        if(c == '\n' || c == '\r') {
          line++;
          pos = 0;
          lineending = c;
        }
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
  
  public int line() {
    return tokenline;
  }
  
  public int pos() {
    return tokenpos;
  }
  
}
