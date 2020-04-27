package unluac.test;

public class LuaSpec {

  public enum NumberFormat {
    DEFAULT,
    FLOAT,
    INT32,
    INT64
  }
  
  public LuaSpec() {
    this.isDefault = true;
    this.version = 0;
    this.numberFormat = NumberFormat.DEFAULT;
    this.strip = false;
  }
  
  public LuaSpec(int version) {
    this.isDefault = false;
    this.version = version;
    this.numberFormat = NumberFormat.DEFAULT;
    this.strip = false;
  }
  
  public String id() {
    String id = "lua";
    id += Integer.toHexString(version);
    return id;
  }
  
  public void setNumberFormat(NumberFormat format) {
    this.numberFormat = format;
  }
  
  public void setStrip(boolean strip) {
    this.strip = strip;
  }
  
  public String getLuaCName() {
    return "lua" + getVersionString() + "c" + getNumberFormatString();
  }
  
  public String[] getArgs() {
    if(strip) {
      return new String[] {"-s"};
    } else {
      return new String[] {};
    }
  }
  
  public boolean compatible(String filename) {
    int version = 0;
    int underscore = filename.indexOf('_');
    if(underscore != -1) {
      String prefix = filename.substring(0, underscore);
      try {
        version = Integer.parseInt(prefix, 16);
      } catch(NumberFormatException e) {}
    }
    return version == 0 || this.version >= version;
  }
  
  private String getVersionString() {
    if(isDefault) {
      return "";
    } else {
      return Integer.toHexString(version);
    }
  }
  
  private String getNumberFormatString() {
    switch(numberFormat) {
      case DEFAULT:
        return "";
      case FLOAT:
        return "_float";
      case INT32:
        return "_int32";
      case INT64:
        return "_int64";
      default:
        throw new IllegalStateException();
    }
  }
  
  private boolean isDefault;
  private int version;
  private NumberFormat numberFormat;
  private boolean strip;
}
