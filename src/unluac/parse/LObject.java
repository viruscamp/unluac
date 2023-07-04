package unluac.parse;


abstract public class LObject extends BObject {

  public String deref() {
    throw new IllegalStateException();
  }
  
  public String toPrintString() {
    throw new IllegalStateException();
  }
  
  public String toShortString() {
    return toPrintString();
  }
  
  abstract public boolean equals(Object o);
  
}
