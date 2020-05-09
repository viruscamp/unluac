package unluac.parse;

import unluac.util.StringUtils;

public class LString extends LObject {

  public final String value;
  public boolean islong;
  
  public LString(String value) {    
    this.value = value;
    islong = false;
  }
  
  @Override
  public String deref() {
    return value;
  }
  
  @Override
  public String toPrintString() {
    String prefix = "";
    if(islong) prefix = "L";
    return prefix + StringUtils.toPrintString(value);
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LString) {
      LString os = (LString) o;
      return os.value.equals(value) && os.islong == islong;
    }
    return false;
  }
  
}
