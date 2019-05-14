package unluac.util;

public class StringUtils {

  public static String toPrintString(String s) {
    if(s == null) return "\"\"";
    StringBuilder b = new StringBuilder();
    b.append('"');
    for(int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      int ci = (int)c;
      if(c == '"') {
        b.append("\\\"");
      } else if(c == '\\') {
        b.append("\\\\");
      } else if(ci >= 32 && ci <= 126) {
        b.append(c);
      } else if(c == '\n') {
        b.append("\\n");
      } else if(c == '\t') {
        b.append("\\t");
      } else if(c == '\r') {
        b.append("\\r");
      } else if(c == '\b') {
        b.append("\\b");
      } else if(c == '\f') {
        b.append("\\f");
      } else if(ci == 11) {
        b.append("\\v");
      } else if(ci == 7) {
        b.append("\\a");
      } else {
        b.append(String.format("\\x%02x", ci));
      }
    }
    b.append('"');
    return b.toString();
  }
  
  public static String fromPrintString(String s) {
    if(s.charAt(0) != '"') throw new IllegalStateException("Bad string " + s);
    if(s.charAt(s.length() - 1) != '"') throw new IllegalStateException("Bad string " + s);
    StringBuilder b = new StringBuilder();
    for(int i = 1; i < s.length() - 1; /* nothing */) {
      char c = s.charAt(i++);
      if(c == '\\') {
        if(i < s.length() - 1) {
          c = s.charAt(i++);
          if(c == '"') {
            b.append('"');
          } else if(c == '\\') {
            b.append('\\');
          } else if(c == 'n') {
            b.append('\n');
          } else if(c == 't') {
            b.append('\t');
          } else if(c == 'r') {
            b.append('\r');
          } else if(c == 'b') {
            b.append('\b');
          } else if(c == 'f') {
            b.append('\f');
          } else if(c == 'v') {
            b.append((char) 11);
          } else if(c == 'a') {
            b.append((char) 7);
          } else if(c == 'x') {
            if(i + 1 < s.length() - 1) {
              String digits = s.substring(i, i + 2);
              i += 2;
              b.append((char) Integer.parseInt(digits, 16));
            } else {
              return null; // error
            }
          } else {
            return null; // error
          }
        } else {
          return null; // error
        }
      } else {
        b.append(c);
      }
    }
    return b.toString();
  }
  
  private StringUtils() {}
}
