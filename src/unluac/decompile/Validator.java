package unluac.decompile;

public class Validator {

	public static void process(Decompiler d) {
		Code code = d.code;
		for(int line = 1; line <= code.length; line++) {
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
