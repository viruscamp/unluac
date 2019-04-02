package unluac.decompile;

public interface CodeExtract {

  int extract_A(int codepoint);

  boolean check_A(int A);
  
  int encode_A(int A);
  
  int extract_C(int codepoint);

  int extract_B(int codepoint);

  int extract_Ax(int codepoint);
  
  int extract_Bx(int codepoint);

  int extract_sBx(int codepoint);

  int extract_op(int codepoint);
  
  boolean check_op(int op);
  
  int encode_op(int op);
  
  boolean is_k(int field);
  
  int get_k(int field);
}
