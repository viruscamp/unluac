package unluac.parse;

import unluac.util.StringUtils;

public class LString extends LObject {

  public static final LString NULL = new LString("");
  
  public final String value;
  public boolean islong;
  
  public LString(String value) {    
    this(value, false);
  }
  
  public LString(String value, boolean islong) {
    this.value = value;
    this.islong = islong;
  }
  
  @Override
  public String deref() {
    return value;
  }
  
  @Override
  public String toPrintString() {
    if(this == NULL) {
      return "null";
    } else {
      String prefix = "";
      if(islong) prefix = "L";
      return prefix + StringUtils.toPrintString(value);
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
