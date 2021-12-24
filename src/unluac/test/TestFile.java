package unluac.test;

public class TestFile {

  public static final int DEFAULT_VERSION = 0x50;
  
  public static final int RELAXED_SCOPE = 1;
  
  public final String name;
  public final int version;
  public final int flags;
  
  public TestFile(String name) {
    this(name, DEFAULT_VERSION, 0);
  }
  
  public TestFile(String name, int version) {
    this(name, version, 0);
  }
  
  public TestFile(String name, int version, int flags) {
    this.name = name;
    this.version = version;
    this.flags = flags;
  }
  
  public boolean getFlag(int flag) {
    return (flags & flag) == flag;
  }
    
}
