package unluac.decompile;

import unluac.Version;

public class CodeExtract {

public static class Field {
    
    public Field(int size, int shift) {
      this(size, shift, 0);
    }
    
    public Field(int size, int shift, int offset) {
      this.shift = shift;
      this.mask = size_to_mask(size);
      this.offset = offset;
    }
    
    public int extract(int codepoint) {
      return ((codepoint >>> shift) & mask) - offset;
    }
    
    public boolean check(int x) {
      return ((x + offset) & ~mask) == 0;
    }
    
    public int encode(int x) {
      return (x + offset) << shift;
    }
    
    private final int shift;
    private final int mask;
    private final int offset;
    
  }
  
  /**
   * Creates standard code extract used for Lua 5.1 through Lua 5.3.
   */
  public CodeExtract(Version version) {
    op = new Field(6, 0);
    A = new Field(8, 6);
    B = new Field(9, 23);
    C = new Field(9, 14);
    Ax = new Field(26, 6);
    Bx = new Field(18, 14);
    sBx = new Field(18, 14, 131071);
    rk_offset = version.getConstantsOffset();
  }

  /**
   * Creates code extract based on header fields for Lua 5.0.
   */
  public CodeExtract(Version version, int sizeOp, int sizeA, int sizeB, int sizeC) {
    op = new Field(sizeOp, 0);
    A = new Field(sizeA, sizeB + sizeC + sizeOp);
    B = new Field(sizeB, sizeB + sizeOp);
    C = new Field(sizeC, sizeOp);
    Ax = null;
    Bx = new Field(sizeB + sizeC, sizeOp);
    sBx = new Field(sizeB + sizeC, sizeOp, size_to_mask(sizeB + sizeC) / 2);
    rk_offset = version.getConstantsOffset();
  }
  
  public boolean is_k(int field) {
    return field >= rk_offset;
  }
  
  public int get_k(int field) {
    return field - rk_offset;
  }

  public final Field op;
  public final Field A;
  public final Field B;
  public final Field C;
  public final Field Ax;
  public final Field Bx;
  public final Field sBx;
  
  private final int rk_offset;
  
  private static int size_to_mask(int size) {
    return (int)((1L << size) - 1);
  }
  
}
