package unluac.decompile;

public class PrintFlag {
  
  public static final int DISASSEMBLER = 0x00000001;
  public static final int SHORT        = 0x00000002;
  
  public static boolean test(int flags, int flag) {
    return (flags & flag) != 0;
  }
  
}
