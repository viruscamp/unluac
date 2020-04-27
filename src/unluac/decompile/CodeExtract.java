package unluac.decompile;

public interface CodeExtract {

  public int extract_A(int codepoint);

  public boolean check_A(int A);
  
  public int encode_A(int A);
  
  public int extract_C(int codepoint);
  
  public boolean check_C(int C);
  
  public int encode_C(int C);

  public int extract_B(int codepoint);
  
  public boolean check_B(int B);
  
  public int encode_B(int B);

  public int extract_Ax(int codepoint);
  
  public int extract_Bx(int codepoint);
  
  public boolean check_Bx(int Bx);
  
  public int encode_Bx(int Bx);

  public int extract_sBx(int codepoint);

  public int extract_op(int codepoint);
  
  public boolean check_op(int op);
  
  public int encode_op(int op);
  
  public boolean is_k(int field);
  
  public int get_k(int field);
}
