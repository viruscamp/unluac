package unluac.parse;

import unluac.util.StringUtils;

public class LString extends LObject {

  public final String value;
  
  public LString(String value) {    
    this.value = value;
  }
  
  @Override
  public String deref() {
    return value;
  }
  
  @Override
  public String toPrintString() {
    return StringUtils.toPrintString(value);
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LString) {
      LString os = (LString) o;
      return os.value.equals(value);
    }
    return false;
  }
  
}
