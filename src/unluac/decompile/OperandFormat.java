package unluac.decompile;

public enum OperandFormat {
  A(Field.A, Format.RAW),
  AR(Field.A, Format.REGISTER),
  AU(Field.A, Format.UPVALUE),
  B(Field.B, Format.RAW),
  BR(Field.B, Format.REGISTER),
  BRK(Field.B, Format.REGISTER_K),
  BU(Field.B, Format.UPVALUE),
  C(Field.C, Format.RAW),
  CR(Field.C, Format.REGISTER),
  CRK(Field.C, Format.REGISTER_K),
  Ax(Field.Ax, Format.RAW),
  Bx(Field.Bx, Format.RAW),
  BxK(Field.Bx, Format.CONSTANT),
  BxF(Field.Bx, Format.FUNCTION),
  sBxJ(Field.sBx, Format.JUMP),
  x(Field.x, Format.RAW);
  
  public final Field field;
  public final Format format;
  
  private OperandFormat(Field field, Format format) {
    this.field = field;
    this.format = format;
  }
  
  public static enum Field {
    A,
    B,
    C,
    Ax,
    Bx,
    sBx,
    x,
  }
  
  public static enum Format {
    RAW,
    REGISTER,
    UPVALUE,
    REGISTER_K,
    CONSTANT,
    FUNCTION,
    JUMP,
  }
}
