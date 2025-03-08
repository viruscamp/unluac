package unluac.decompile;

public class Validator {

  public static void process(Function f, Registers r) {
    Code code = f.code;
    
    switch(code.op(code.length)) {
      case RETURN:
      case RETURN54:
        if(code.B(code.length) != 1) throw new DecompilerException(f, code.length, "Implicit return must have zero values");
        break;
      case RETURN0:
        break;
      default:
        throw new DecompilerException(f, code.length, "Function doesn't end with implicit return");
    }
    
    // Having validated final op allows look-ahead
    
    for(int line = 1; line <= code.length; line++) {
      Op op = code.op(line);
      if(op == null) throw new DecompilerException(f, line, "Unknown opcode: " + code.opcode(line));
      if(op.hasJump()) {
        int target = code.target(line);
        if(target < 1 || target > code.length) throw new DecompilerException(f, line, "Jump target out of range: " + target);
      }
      if(op.isCondition() && !code.op(line + 1).isJmp()) throw new DecompilerException(f, line, "Condition is not followed by jump");
      for(OperandFormat operand : op.operands) {
        int x = code.field(operand.field, line);
        switch(operand.format) {
          case RAW:
            // always okay (well...)
            break;
          case REGISTER:
            if(x > r.registers) throw new DecompilerException(f, line, "Register r" + x + " is out of range");
            break;
          case CONSTANT:
            if(x > f.constants.length) throw new DecompilerException(f, line, "Constant k" + x + " is out of range");
            break;
          default:
            break;
        }
      }
    }
  }
  
  //static only
  private Validator() {}
	
}
