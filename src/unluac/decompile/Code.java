package unluac.decompile;

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
   * Returns the B field of the instruction at the given line.
   */
  public int B(int line) {
    return extractor.B.extract(code[line - 1]);
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
   * Returns the absolute target address of a jump instruction (using sBx) and the given line.
   */
  public int target(int line) {
    return line + 1 + sBx(line);
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
