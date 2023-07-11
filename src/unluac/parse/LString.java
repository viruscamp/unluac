package unluac.parse;

import unluac.decompile.PrintFlag;
import unluac.util.StringUtils;

public class LString extends LObject {

  public static final LString NULL = new LString("");
  
  public final String value;
  public final char terminator;
  public boolean islong;
  
  public LString(String value) {    
    this(value, '\0', false);
  }
  
  public LString(String value, char terminator) {
    this(value, terminator, false);
  }
  
  public LString(String value, char terminator, boolean islong) {
    this.value = value;
    this.terminator = terminator;
    this.islong = islong;
  }
  
  @Override
  public String deref() {
    return value;
  }
  
  @Override
  public String toPrintString(int flags) {
    if(this == NULL) {
      return "null";
    } else {
      String prefix = "";
      String suffix = "";
      if(islong) prefix = "L";
      if(PrintFlag.test(flags, PrintFlag.SHORT)) {
        final int LIMIT = 20;
        if(value.length() > LIMIT) suffix = " (truncated)";
        return prefix + StringUtils.toPrintString(value, LIMIT) + suffix;
      } else {
        return prefix + StringUtils.toPrintString(value);
      }
    }
  }
  
  @Override
  public boolean equals(Object o) {
    if(this == NULL || o == NULL) {
      return this == o;
    } else if(o instanceof LString) {
      LString os = (LString) o;
      return os.value.equals(value) && os.islong == islong;
    }
    return false;
  }
  
}
