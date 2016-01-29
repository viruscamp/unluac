package unluac.decompile;

public enum Op {
  // Lua 5.1 Opcodes
  MOVE(OpcodeFormat.A_B),
  LOADK(OpcodeFormat.A_Bx),
  LOADBOOL(OpcodeFormat.A_B_C),
  LOADNIL(OpcodeFormat.A_B),
  GETUPVAL(OpcodeFormat.A_B),
  GETGLOBAL(OpcodeFormat.A_Bx),
  GETTABLE(OpcodeFormat.A_B_C),
  SETGLOBAL(OpcodeFormat.A_Bx),
  SETUPVAL(OpcodeFormat.A_B),
  SETTABLE(OpcodeFormat.A_B_C),
  NEWTABLE(OpcodeFormat.A_B_C),
  SELF(OpcodeFormat.A_B_C),
  ADD(OpcodeFormat.A_B_C),
  SUB(OpcodeFormat.A_B_C),
  MUL(OpcodeFormat.A_B_C),
  DIV(OpcodeFormat.A_B_C),
  MOD(OpcodeFormat.A_B_C),
  POW(OpcodeFormat.A_B_C),
  UNM(OpcodeFormat.A_B),
  NOT(OpcodeFormat.A_B),
  LEN(OpcodeFormat.A_B),
  CONCAT(OpcodeFormat.A_B_C),
  JMP(OpcodeFormat.sBx), // TODO: Different in 5.2
  EQ(OpcodeFormat.A_B_C),
  LT(OpcodeFormat.A_B_C),
  LE(OpcodeFormat.A_B_C),
  TEST(OpcodeFormat.A_C),
  TESTSET(OpcodeFormat.A_B_C),
  CALL(OpcodeFormat.A_B_C),
  TAILCALL(OpcodeFormat.A_B_C),
  RETURN(OpcodeFormat.A_B),
  FORLOOP(OpcodeFormat.A_sBx),
  FORPREP(OpcodeFormat.A_sBx),
  TFORLOOP(OpcodeFormat.A_C),
  SETLIST(OpcodeFormat.A_B_C),
  CLOSE(OpcodeFormat.A),
  CLOSURE(OpcodeFormat.A_Bx),
  VARARG(OpcodeFormat.A_B),
  // Lua 5.2 Opcodes
  LOADNIL52(OpcodeFormat.A_B),
  LOADKX(OpcodeFormat.A),
  GETTABUP(OpcodeFormat.A_B_C),
  SETTABUP(OpcodeFormat.A_B_C),
  SETLIST52(OpcodeFormat.A_B_C),
  TFORCALL(OpcodeFormat.A_C),
  EXTRAARG(OpcodeFormat.Ax),
  // Lua 5.0 Opcodes
  NEWTABLE50(OpcodeFormat.A_B_C),
  SETLIST50(OpcodeFormat.A_B_C),
  SETLISTO(OpcodeFormat.A_Bx),
  TFORPREP(OpcodeFormat.A_sBx),
  TEST50(OpcodeFormat.A_B_C),
  // Lua 5.3 Opcodes
  IDIV(OpcodeFormat.A_B_C),
  BAND(OpcodeFormat.A_B_C),
  BOR(OpcodeFormat.A_B_C),
  BXOR(OpcodeFormat.A_B_C),
  SHL(OpcodeFormat.A_B_C),
  SHR(OpcodeFormat.A_B_C),
  BNOT(OpcodeFormat.A_B),
  // Special
  EXTRABYTE(OpcodeFormat.ALL);
  
  private final OpcodeFormat format;
  
  private Op(OpcodeFormat format) {
    this.format = format;
  }
  
  /**
   * SETLIST sometimes uses an extra byte without tagging it.
   * This means that the value in the extra byte can be detected as any other opcode unless it is recognzied.
   */
  public boolean hasExtraByte(int codepoint, CodeExtract ex) {
    if(this == Op.SETLIST) {
      return ex.extract_C(codepoint) == 0;
    } else {
      return false;
    }
  }
  
  /**
   * Returns the target register of the instruction at the given
   * line or -1 if the instruction does not have a unique target.
   */
  public int target(int codepoint, CodeExtract ex) {
    switch(this) {
      case MOVE:
      case LOADK:
      case LOADKX:
      case LOADBOOL:
      case GETUPVAL:
      case GETTABUP:
      case GETGLOBAL:
      case GETTABLE:
      case NEWTABLE:
      case NEWTABLE50:
      case ADD:
      case SUB:
      case MUL:
      case DIV:
      case MOD:
      case POW:
      case IDIV:
      case BAND:
      case BOR:
      case BXOR:
      case SHL:
      case SHR:
      case UNM:
      case NOT:
      case LEN:
      case BNOT:
      case CONCAT:
      case CLOSURE:
      case TESTSET:
      case TEST50:
        return ex.extract_A(codepoint);
      case LOADNIL:
        if(ex.extract_A(codepoint) == ex.extract_B(codepoint)) {
          return ex.extract_A(codepoint);
        } else {
          return -1;
        }
      case LOADNIL52:
        if(ex.extract_B(codepoint) == 0) {
          return ex.extract_A(codepoint);
        } else {
          return -1;
        }
      case SETGLOBAL:
      case SETUPVAL:
      case SETTABUP:
      case SETTABLE:
      case JMP:
      case TAILCALL:
      case RETURN:
      case FORLOOP:
      case FORPREP:
      case TFORPREP:
      case TFORCALL:
      case TFORLOOP:
      case CLOSE:
      case EXTRAARG:
      case SELF:
      case EQ:
      case LT:
      case LE:
      case TEST:
      case SETLIST:
      case SETLIST52:
      case SETLIST50:
      case SETLISTO:
        return -1;
      case CALL: {
        int a = ex.extract_A(codepoint);
        int c = ex.extract_C(codepoint);
        if(c == 2) {
          return a;
        } else {
          return -1; 
        }
      }
      case VARARG: {
        int a = ex.extract_A(codepoint);
        int b = ex.extract_B(codepoint);
        if(b == 1) {
          return a;
        } else {
          return -1;
        }
      }
      case EXTRABYTE:
        return -1;
    }
    throw new IllegalStateException();
  }
  
  public String codePointToString(int codepoint, CodeExtract ex) {
    switch(format) {
      case A:
        return this.name() + " " + ex.extract_A(codepoint);
      case A_B:
        return this.name() + " " + ex.extract_A(codepoint) + " " + ex.extract_B(codepoint);
      case A_C:
        return this.name() + " " + ex.extract_A(codepoint) + " " + ex.extract_C(codepoint);
      case A_B_C:
        return this.name() + " " + ex.extract_A(codepoint) + " " + ex.extract_B(codepoint) + " " + ex.extract_C(codepoint);
      case A_Bx:
        return this.name() + " " + ex.extract_A(codepoint) + " " + ex.extract_Bx(codepoint);
      case A_sBx:
        return this.name() + " " + ex.extract_A(codepoint) + " " + ex.extract_sBx(codepoint);
      case Ax:
        return this.name() + " <Ax>"; 
      case sBx:
        return this.name() + " " + ex.extract_sBx(codepoint);
      default:
        return this.name();
    }
  }
}
