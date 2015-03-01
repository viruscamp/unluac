package unluac.decompile;

import unluac.parse.LFunction;

public class Code {
  
  public static CodeExtract Code51 = new CodeExtract() {

    /**
     * Returns the A field of the given codepoint.
     */
    @Override
    public int extract_A(int codepoint) {
      return (codepoint >> 6) & 0x0000000FF;
    }

    /**
     * Returns the C field of the given codepoint.
     */
    @Override
    public int extract_C(int codepoint) {
      return (codepoint >> 14) & 0x000001FF;
    }

    /**
     * Returns the B field of the given codepoint.
     */
    @Override
    public int extract_B(int codepoint) {
      return codepoint >>> 23;
    }

    /**
     * Returns the Bx (B extended) field of the given codepoint.
     */
    @Override
    public int extract_Bx(int codepoint) {
      return codepoint >>> 14;
    }

    /**
     * Returns the sBx (signed B extended) field of the given codepoint.
     */
    @Override
    public int extract_sBx(int codepoint) {
      return (codepoint >>> 14) - 131071;
    }

    @Override
    public int extract_op(int codepoint) {
      return codepoint & 0x0000003F;
    }

  };
  
  private final CodeExtract extractor;
  private final OpcodeMap map;
  private final int[] code;
  public final int length;
  
  public Code(LFunction function) {
    this.code = function.code;
    this.length = code.length;
    map = function.header.version.getOpcodeMap();
    extractor = function.header.extractor;
  }
  
  //public boolean reentered = false;
  
  /**
   * Returns the operation indicated by the instruction at the given line.
   */
  public Op op(int line) {
    /*if(!reentered) {
      reentered = true;
      System.out.println("line " + line + ": " + toString(line));
      reentered = false;
    }*/
    return map.get(opcode(line));
  }
  
  public int opcode(int line) {
    return code[line - 1] & 0x0000003F;
  }
  
  /**
   * Returns the A field of the instruction at the given line.
   */
  public int A(int line) {
    return extractor.extract_A(code[line - 1]);
  }
  
  /**
   * Returns the C field of the instruction at the given line.
   */
  public int C(int line) {
    return extractor.extract_C(code[line - 1]);
  }
  
  /**
   * Returns the B field of the instruction at the given line.
   */
  public int B(int line) {
    return extractor.extract_B(code[line - 1]);
  }
  
  /**
   * Returns the Bx field (B extended) of the instruction at the given line.
   */
  public int Bx(int line) {
    return extractor.extract_Bx(code[line - 1]);
  }
  
  /**
   * Returns the sBx field (signed B extended) of the instruction at the given line.
   */
  public int sBx(int line) {
    return extractor.extract_sBx(code[line - 1]);
  }
  
  /**
   * Returns the absolute target address of a jump instruction (using sBx) and the given line.
   */
  public int target(int line) {
    return line + 1 + extractor.extract_sBx(code[line - 1]);
  }
  
  /**
   * Returns the full instruction codepoint at the given line.
   */
  public int codepoint(int line) {
    return code[line - 1];
  }
  
  public int length() {
    return code.length;
  }
  
  public String toString(int line) {
    return op(line).codePointToString(codepoint(line), extractor);
  }
  
}
