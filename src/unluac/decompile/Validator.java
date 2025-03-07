package unluac.decompile;

public class Validator {

  public static void process(Function f) {
    Code code = f.code;
    for(int line = 1; line <= code.length; line++) {
      Op op = code.op(line);
      if(op == null) throw new RuntimeException(f.fullDisassemblerName() + " " + line + ": Unknown opcode " + code.opcode(line));
      switch(code.op(line)) {
        case EQ: {
          /* TODO
            AssertionManager.assertCritical(
                line + 1 <= code.length && code.isJMP(line + 1),
                "ByteCode validation failed; EQ instruction is not followed by JMP"
            );
            break;*/
        }
        case LT: {
            break;
        }
        default:
          break;
      }
    }
  }
  	
  //static only
  private Validator() {}
	
}
