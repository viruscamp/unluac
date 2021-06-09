package unluac.decompile;

import unluac.Version;
import unluac.parse.LFunction;

public class Code {
  
  private final CodeExtract extractor;
  private final OpcodeMap map;
  private final int[] code;
  private final boolean[] extraByte;
  private final boolean[] upvalue;
  public final int length;
  
  public Code(LFunction function) {
    this.code = function.code;
    this.length = code.length;
    map = function.header.opmap;
    extractor = function.header.extractor;
    extraByte = new boolean[length];
    for(int i = 0; i < length; i++) {
      int line = i + 1;
      extraByte[i] = op(line).hasExtraByte(codepoint(line), extractor);
    }
    upvalue = new boolean[length];
    if(function.header.version.upvaluedeclarationtype.get() == Version.UpvalueDeclarationType.INLINE) {
      for(int i = 0; i < length; i++) {
        int line = i + 1;
        if(op(line) == Op.CLOSURE) {
          int f = Bx(line);
          if(f < function.functions.length) {
            int nups = function.functions[f].numUpvalues;
            for(int j = 1; j <= nups; j++) {
              if(i + j < length) {
                upvalue[i + j] = true;
              }
            }
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
    return extractor.op.extract(code[line - 1]);
  }
  
  /**
   * Returns the A field of the instruction at the given line.
   */
  public int A(int line) {
    return extractor.A.extract(code[line - 1]);
  }
  
  /**
   * Returns the C field of the instruction at the given line.
   */
  public int C(int line) {
    return extractor.C.extract(code[line - 1]);
  }
  
  /**
   * Returns the sC (signed C) field of the instruction at the given line.
   */
  public int sC(int line) {
    int C = C(line);
    return C - extractor.C.max() / 2;
  }
  
  
  /**
   * Returns the k field of the instruction at the given line (1 is true, 0 is false).
   */
  public boolean k(int line) {
    return extractor.k.extract(code[line - 1]) != 0;
  }
  
  /**
   * Returns the B field of the instruction at the given line.
   */
  public int B(int line) {
    return extractor.B.extract(code[line - 1]);
  }
  
  /**
   * Returns the sB (signed B) field of the instruction at the given line.
   */
  public int sB(int line) {
    int B = B(line);
    return B - extractor.B.max() / 2;
  }
  
  /**
   * Returns the Ax field (A extended) of the instruction at the given line.
   */
  public int Ax(int line) {
    return extractor.Ax.extract(code[line - 1]);
  }
  
  /**
   * Returns the Bx field (B extended) of the instruction at the given line.
   */
  public int Bx(int line) {
    return extractor.Bx.extract(code[line - 1]);
  }
  
  /**
   * Returns the sBx field (signed B extended) of the instruction at the given line.
   */
  public int sBx(int line) {
    return extractor.sBx.extract(code[line - 1]);
  }
  
  /**
   * Returns the absolute target address of a jump instruction and the given line.
   * This field will be chosen automatically based on the opcode.
   */
  public int target(int line) {
    return line + 1 + op(line).jumpField(codepoint(line), extractor);
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
