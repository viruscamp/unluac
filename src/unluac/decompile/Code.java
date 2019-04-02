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
    
    @Override
    public boolean check_A(int A) {
      return (A & ~0x000000FF) == 0;
    }
    
    @Override
    public int encode_A(int A) {
      return A << 6;
    }

    /**
     * Returns the C field of the given codepoint.
     */
    @Override
    public int extract_C(int codepoint) {
      return (codepoint >> 14) & 0x000001FF;
    }
    
    public boolean check_C(int C) {
      return (C & ~0x000001FF) == 0;
    }
    
    public int encode_C(int C) {
      return C << 14;
    }

    /**
     * Returns the B field of the given codepoint.
     */
    @Override
    public int extract_B(int codepoint) {
      return codepoint >>> 23;
    }
    
    public boolean check_B(int B) {
      return (B & ~0x000001FF) == 0;
    }
    
    public int encode_B(int B) {
      return B << 23;
    }

    /**
     * Returns the Ax (A extended) field of the given codepoint.
     */
    @Override
    public int extract_Ax(int codepoint) {
      return codepoint >>> 6;
    }
    
    public boolean check_Ax(int Ax) {
      return (Ax & ~0x03FFFFFF) == 0;
    }
    
    public int encode_Ax(int Ax) {
      return Ax << 6;
    }
    
    /**
     * Returns the Bx (B extended) field of the given codepoint.
     */
    @Override
    public int extract_Bx(int codepoint) {
      return codepoint >>> 14;
    }
    
    public boolean check_Bx(int Bx) {
      return (Bx & ~0x0003FFFF) == 0;
    }
    
    public int encode_Bx(int Bx) {
      return Bx << 14;
    }

    /**
     * Returns the sBx (signed B extended) field of the given codepoint.
     */
    @Override
    public int extract_sBx(int codepoint) {
      return (codepoint >>> 14) - 131071;
    }
    
    public boolean check_sBx(int sBx) {
      return ((sBx + 131071) & ~0x0003FFFF) == 0;
    }
    
    public int encode_sBx(int sBx) {
      return (sBx + 131071) << 14;
    }

    @Override
    public int extract_op(int codepoint) {
      return codepoint & 0x0000003F;
    }
    
    public boolean check_op(int opcode) {
      return (opcode & ~0x0000003F) == 0;
    }
    
    public int encode_op(int opcode) {
      return opcode;
    }
    
    @Override
    public boolean is_k(int field) {
      return field >= 256;
    }
    
    @Override
    public int get_k(int field) {
      return field - 256;
    }

  };
  
  private final CodeExtract extractor;
  private final OpcodeMap map;
  private final int[] code;
  private final boolean[] extraByte;
  private final boolean[] upvalue;
  public final int length;
  
  public Code(LFunction function) {
    this.code = function.code;
    this.length = code.length;
    map = function.header.version.getOpcodeMap();
    extractor = function.header.extractor;
    extraByte = new boolean[length];
    for(int i = 0; i < length; i++) {
      int line = i + 1;
      extraByte[i] = op(line).hasExtraByte(codepoint(line), extractor);
    }
    upvalue = new boolean[length];
    if(function.header.version.usesInlineUpvalueDeclarations()) {
      for(int i = 0; i < length; i++) {
        int line = i + 1;
        if(op(line) == Op.CLOSURE) {
          int nups = function.functions[Bx(line)].numUpvalues;
          for(int j = 1; j <= nups; j++) {
            upvalue[i + j] = true;
          }
        }
      }
    }
  }
  
  public CodeExtract getExtractor() {
    return extractor;
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
    if(line >= 2 && extraByte[line - 2]) {
      return Op.EXTRABYTE;
    } else {
      return map.get(opcode(line));
    }
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
   * Returns the Ax field (A extended) of the instruction at the given line.
   */
  public int Ax(int line) {
    return extractor.extract_Ax(code[line - 1]);
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
  
  public boolean isUpvalueDeclaration(int line) {
    return upvalue[line - 1];
  }
  
  public int length() {
    return code.length;
  }
  
  public String toString(int line) {
    return op(line).codePointToString(codepoint(line), extractor, null);
  }
  
}
