package unluac.parse;

import unluac.Version;


public class LString extends LObject {

  public final String value;
  public final boolean reserved;
  
  public LString(Version version, String value) {    
    this.value = value;
    this.reserved = version.isReserved(this.value);
  }
  
  @Override
  public String deref() {
    return value;
  }
  
  public boolean reserved() {
    return reserved;
  }
  
  @Override
  public String toString() {
    // TODO: yikes
    return "\"" + value + "\"";
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
