package unluac.decompile;

import unluac.Version;

public class CodeExtract {

public static class Field {
    
    public Field(int size, int shift) {
      this(size, shift, 0);
    }
    
    public Field(int size, int shift, int offset) {
      this.size = size;
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
    
    public int clear(int codepoint) {
      return codepoint & ~(mask << shift);
    }
    
    public int max() {
      return mask - offset;
    }
    
    public final int size;
    private final int shift;
    private final int mask;
    private final int offset;
    
  }
  
  public CodeExtract(Version version, int sizeOp, int sizeA, int sizeB, int sizeC) {
    switch(version.instructionformat.get()) {
      case LUA50:
        op = new Field(sizeOp, 0);
        A = new Field(sizeA, sizeB + sizeC + sizeOp);
        B = new Field(sizeB, sizeB + sizeOp);
        C = new Field(sizeC, sizeOp);
        k = null;
        Ax = null;
        sJ = null;
        Bx = new Field(sizeB + sizeC, sizeOp);
        sBx = new Field(sizeB + sizeC, sizeOp, size_to_mask(sizeB + sizeC) / 2);
        x = new Field(32, 0);
        break;
      case LUA51:
        op = new Field(6, 0);
        A = new Field(8, 6);
        B = new Field(9, 23);
        C = new Field(9, 14);
        k = null;
        Ax = new Field(26, 6);
        sJ = null;
        Bx = new Field(18, 14);
        sBx = new Field(18, 14, 131071);
        x = new Field(32, 0);
        break;
      case LUA54:
        op = new Field(7, 0);
        A = new Field(8, 7);
        B = new Field(8, 16);
        C = new Field(8, 24);
        k = new Field(1, 15);
        Ax = new Field(25, 7);
        sJ = new Field(25, 7, (1 << 24) - 1);
        Bx = new Field(17, 15);
        sBx = new Field(17, 15, (1 << 16) - 1);
        x = new Field(32, 0);
        break;
      default:
        throw new IllegalStateException();
    }
    Integer rk_offset = version.rkoffset.get();
    this.rk_offset = (rk_offset == null) ? -1 : rk_offset;
  }

  public boolean is_k(int field) {
    return field >= rk_offset;
  }
  
  public int get_k(int field) {
    return field - rk_offset;
  }
  
  public int encode_k(int constant) {
    return constant + rk_offset;
  }

  public final Field op;
  public final Field A;
  public final Field B;
  public final Field C;
  public final Field k;
  public final Field Ax;
  public final Field sJ;
  public final Field Bx;
  public final Field sBx;
  public final Field x;
  
  private final int rk_offset;
  
  private static int size_to_mask(int size) {
    return (int)((1L << size) - 1);
  }
  
}
