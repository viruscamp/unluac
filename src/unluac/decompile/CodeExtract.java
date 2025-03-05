package unluac.decompile;

import unluac.Version;

public class CodeExtract {

  public static final int BITFIELD_OPCODE = 1;
  public static final int BITFIELD_A = 2;
  public static final int BITFIELD_B = 4;
  public static final int BITFIELD_C = 8;
  public static final int BITFIELD_K = 16;
  
  public static final int BITFIELD_BX = BITFIELD_B | BITFIELD_C | BITFIELD_K;
  public static final int BITFIELD_AX = BITFIELD_A | BITFIELD_B | BITFIELD_C | BITFIELD_K;
  public static final int BITFIELD_X = BITFIELD_OPCODE | BITFIELD_A | BITFIELD_B | BITFIELD_C | BITFIELD_K;
  
  public static class Field {
  
    public Field(int slot, int size, int shift) {
      this(slot, size, shift, 0);
    }
    
    public Field(int slot, int size, int shift, int offset) {
      this.slot = slot;
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
    
    public int mask() {
      return mask << shift;
    }
    
    public String defaultname() {
      switch(slot) {
        case BITFIELD_A: return "a";
        case BITFIELD_B: return "b";
        case BITFIELD_C: return "c";
        case BITFIELD_K: return "k";
        default: throw new IllegalStateException();
      }
    }
    
    public final int slot;
    public final int size;
    private final int shift;
    private final int mask;
    private final int offset;
    
  }
  
  public CodeExtract(Version version, int sizeOp, int sizeA, int sizeB, int sizeC) {
    switch(version.instructionformat.get()) {
      case LUA50:
        op = new Field(BITFIELD_OPCODE, sizeOp, 0);
        A = new Field(BITFIELD_A, sizeA, sizeB + sizeC + sizeOp);
        B = new Field(BITFIELD_B, sizeB, sizeB + sizeOp);
        C = new Field(BITFIELD_C, sizeC, sizeOp);
        k = null;
        Ax = null;
        sJ = null;
        Bx = new Field(BITFIELD_BX, sizeB + sizeC, sizeOp);
        sBx = new Field(BITFIELD_BX, sizeB + sizeC, sizeOp, size_to_mask(sizeB + sizeC) / 2);
        x = new Field(BITFIELD_X, 32, 0);
        break;
      case LUA51:
        op = new Field(BITFIELD_OPCODE, 6, 0);
        A = new Field(BITFIELD_A, 8, 6);
        B = new Field(BITFIELD_B, 9, 23);
        C = new Field(BITFIELD_C, 9, 14);
        k = null;
        Ax = new Field(BITFIELD_AX, 26, 6);
        sJ = null;
        Bx = new Field(BITFIELD_BX, 18, 14);
        sBx = new Field(BITFIELD_BX, 18, 14, 131071);
        x = new Field(BITFIELD_X, 32, 0);
        break;
      case LUA54:
        op = new Field(BITFIELD_OPCODE, 7, 0);
        A = new Field(BITFIELD_A, 8, 7);
        B = new Field(BITFIELD_B, 8, 16);
        C = new Field(BITFIELD_C, 8, 24);
        k = new Field(BITFIELD_K, 1, 15);
        Ax = new Field(BITFIELD_AX, 25, 7);
        sJ = new Field(BITFIELD_AX, 25, 7, (1 << 24) - 1);
        Bx = new Field(BITFIELD_BX, 17, 15);
        sBx = new Field(BITFIELD_BX, 17, 15, (1 << 16) - 1);
        x = new Field(BITFIELD_X, 32, 0);
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
  
  public Field get_field(OperandFormat.Field f) {
    switch(f) {
      case A: return A;
      case B: return B;
      case C: return C;
      case k: return k;
      case Ax: return Ax;
      case sJ: return sJ;
      case Bx: return Bx;
      case sBx: return sBx;
      case x: return x;
      default: throw new IllegalStateException("Unhandled field: " + f);
      }
  }
  
  public Field get_field_for_slot(int slot) {
    if((slot & BITFIELD_OPCODE) == 0) return op; 
    if((slot & BITFIELD_A) == 0) return A;
    if((slot & BITFIELD_B) == 0) return B;
    if((slot & BITFIELD_C) == 0) return C;
    if((slot & BITFIELD_K) == 0) return k; // may be null, okay when checking last
    return null;
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
