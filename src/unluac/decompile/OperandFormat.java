package unluac.decompile;

public enum OperandFormat {
  A(Field.A, Format.RAW),
  AR(Field.A, Format.REGISTER),
  AU(Field.A, Format.UPVALUE),
  B(Field.B, Format.RAW),
  BR(Field.B, Format.REGISTER),
  BRK(Field.B, Format.REGISTER_K),
  BK(Field.B, Format.CONSTANT),
  BKS(Field.B, Format.CONSTANT_STRING),
  BI(Field.B, Format.IMMEDIATE_INTEGER),
  BsI(Field.B, Format.IMMEDIATE_SIGNED_INTEGER),
  BU(Field.B, Format.UPVALUE),
  C(Field.C, Format.RAW),
  CR(Field.C, Format.REGISTER),
  CRK(Field.C, Format.REGISTER_K),
  CRK54(Field.C, Format.REGISTER_K54),
  CK(Field.C, Format.CONSTANT),
  CKI(Field.C, Format.CONSTANT_INTEGER),
  CKS(Field.C, Format.CONSTANT_STRING),
  CI(Field.C, Format.IMMEDIATE_INTEGER),
  CsI(Field.C, Format.IMMEDIATE_SIGNED_INTEGER),
  k(Field.k, Format.RAW),
  Ax(Field.Ax, Format.RAW),
  sJ(Field.sJ, Format.JUMP),
  Bx(Field.Bx, Format.RAW),
  BxK(Field.Bx, Format.CONSTANT),
  BxJ(Field.Bx, Format.JUMP),
  BxJ1(Field.Bx, Format.JUMP, 1),
  BxJn(Field.Bx, Format.JUMP_NEGATIVE),
  BxF(Field.Bx, Format.FUNCTION),
  sBxJ(Field.sBx, Format.JUMP),
  sBxI(Field.sBx, Format.IMMEDIATE_INTEGER),
  sBxF(Field.sBx, Format.IMMEDIATE_FLOAT),
  x(Field.x, Format.RAW);
  
  public final Field field;
  public final Format format;
  public final int offset;
  
  private OperandFormat(Field field, Format format) {
    this(field, format, 0); 
  }
  
  private OperandFormat(Field field, Format format, int offset) {
    this.field = field;
    this.format = format;
    this.offset = offset;
  }
  
  public static enum Field {
    A,
    B,
    C,
    k,
    Ax,
    sJ,
    Bx,
    sBx,
    x,
  }
  
  public static enum Format {
    RAW,
    REGISTER,
    UPVALUE,
    REGISTER_K,
    REGISTER_K54,
    CONSTANT,
    CONSTANT_INTEGER,
    CONSTANT_STRING,
    FUNCTION,
    IMMEDIATE_INTEGER,
    IMMEDIATE_SIGNED_INTEGER,
    IMMEDIATE_FLOAT,
    JUMP,
    JUMP_NEGATIVE,
  }
}
