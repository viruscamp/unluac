package unluac.decompile;

public enum Op {
  // Lua 5.1 Opcodes
  MOVE(OperandFormat.AR, OperandFormat.BR),
  LOADK(OperandFormat.AR, OperandFormat.BxK),
  LOADBOOL(OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  LOADNIL(OperandFormat.AR, OperandFormat.BR),
  GETUPVAL(OperandFormat.AR, OperandFormat.BU),
  GETGLOBAL(OperandFormat.AR, OperandFormat.BxK),
  GETTABLE(OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK),
  SETGLOBAL(OperandFormat.AR, OperandFormat.BxK),
  SETUPVAL(OperandFormat.AR, OperandFormat.BU),
  SETTABLE(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  NEWTABLE(OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  SELF(OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK),
  ADD(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  SUB(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  MUL(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  DIV(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  MOD(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  POW(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  UNM(OperandFormat.AR, OperandFormat.BR),
  NOT(OperandFormat.AR, OperandFormat.BR),
  LEN(OperandFormat.AR, OperandFormat.BR),
  CONCAT(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  JMP(OperandFormat.sBxJ),
  EQ(OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
  LT(OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
  LE(OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
  TEST(OperandFormat.AR, OperandFormat.C),
  TESTSET(OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
  CALL(OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  TAILCALL(OperandFormat.AR, OperandFormat.B),
  RETURN(OperandFormat.AR, OperandFormat.B),
  FORLOOP(OperandFormat.AR, OperandFormat.sBxJ),
  FORPREP(OperandFormat.AR, OperandFormat.sBxJ),
  TFORLOOP(OperandFormat.AR, OperandFormat.C),
  SETLIST(OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  CLOSE(OperandFormat.AR),
  CLOSURE(OperandFormat.AR, OperandFormat.BxF),
  VARARG(OperandFormat.AR, OperandFormat.B),
  // Lua 5.2 Opcodes
  JMP52(OperandFormat.A, OperandFormat.sBxJ),
  LOADNIL52(OperandFormat.AR, OperandFormat.B),
  LOADKX(OperandFormat.AR),
  GETTABUP(OperandFormat.AR, OperandFormat.BU, OperandFormat.CRK),
  SETTABUP(OperandFormat.AU, OperandFormat.BRK, OperandFormat.CRK),
  SETLIST52(OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  TFORCALL(OperandFormat.AR, OperandFormat.C),
  TFORLOOP52(OperandFormat.AR, OperandFormat.sBxJ),
  EXTRAARG(OperandFormat.Ax),
  // Lua 5.0 Opcodes
  NEWTABLE50(OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  SETLIST50(OperandFormat.AR, OperandFormat.Bx),
  SETLISTO(OperandFormat.AR, OperandFormat.Bx),
  TFORPREP(OperandFormat.AR, OperandFormat.sBxJ),
  TEST50(OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
  // Lua 5.3 Opcodes
  IDIV(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BAND(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BOR(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BXOR(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  SHL(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  SHR(OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BNOT(OperandFormat.AR, OperandFormat.BR),
  // Lua 5.4 Opcodes
  LOADI(OperandFormat.AR, OperandFormat.sBxI),
  LOADF(OperandFormat.AR, OperandFormat.sBxF),
  LOADFALSE(OperandFormat.AR),
  LFALSESKIP(OperandFormat.AR),
  LOADTRUE(OperandFormat.AR),
  GETTABUP54(OperandFormat.AR, OperandFormat.BU, OperandFormat.CKS),
  GETTABLE54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  GETI(OperandFormat.AR, OperandFormat.BR, OperandFormat.CI),
  GETFIELD(OperandFormat.AR, OperandFormat.BR, OperandFormat.CKS),
  SETTABUP54(OperandFormat.AU, OperandFormat.BK, OperandFormat.CRK54),
  SETTABLE54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK54),
  SETI(OperandFormat.AR, OperandFormat.BI, OperandFormat.CRK54),
  SETFIELD(OperandFormat.AR, OperandFormat.BKS, OperandFormat.CRK54),
  NEWTABLE54(OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  SELF54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK54),
  ADDI(OperandFormat.AR, OperandFormat.BR, OperandFormat.CsI),
  ADDK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  SUBK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  MULK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  MODK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  POWK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  DIVK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  IDIVK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  BANDK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
  BORK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
  BXORK(OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
  SHRI(OperandFormat.AR, OperandFormat.BR, OperandFormat.CsI),
  SHLI(OperandFormat.AR, OperandFormat.CsI, OperandFormat.BR),
  ADD54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  SUB54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  MUL54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  MOD54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  POW54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  DIV54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  IDIV54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  BAND54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  BOR54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  BXOR54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  SHL54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  SHR54(OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  MMBIN(OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
  MMBINI(OperandFormat.AR, OperandFormat.BsI, OperandFormat.C, OperandFormat.k),
  MMBINK(OperandFormat.AR, OperandFormat.BK, OperandFormat.C, OperandFormat.k),
  CONCAT54(OperandFormat.AR, OperandFormat.B),
  TBC(OperandFormat.AR),
  JMP54(OperandFormat.sJ),
  EQ54(OperandFormat.AR, OperandFormat.BR, OperandFormat.k),
  LT54(OperandFormat.AR, OperandFormat.BR, OperandFormat.k),
  LE54(OperandFormat.AR, OperandFormat.BR, OperandFormat.k),
  EQK(OperandFormat.AR, OperandFormat.BK, OperandFormat.k),
  EQI(OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  LTI(OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  LEI(OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  GTI(OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  GEI(OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  TEST54(OperandFormat.AR, OperandFormat.k),
  TESTSET54(OperandFormat.AR, OperandFormat.BR, OperandFormat.k),
  TAILCALL54(OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  RETURN54(OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  RETURN0(OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  RETURN1(OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  FORLOOP54(OperandFormat.AR, OperandFormat.BxJn),
  FORPREP54(OperandFormat.AR, OperandFormat.BxJ),
  TFORPREP54(OperandFormat.AR, OperandFormat.BxJ),
  TFORCALL54(OperandFormat.AR, OperandFormat.C),
  TFORLOOP54(OperandFormat.AR, OperandFormat.BxJn),
  SETLIST54(OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  VARARG54(OperandFormat.AR, OperandFormat.C),
  VARARGPREP(OperandFormat.A),
  // Special
  EXTRABYTE(OperandFormat.x);
  
  public final OperandFormat[] operands;
  
  private Op() {
    this.operands = new OperandFormat[] {};
  }
  
  private Op(OperandFormat f1) {
    this.operands = new OperandFormat[] {f1};
  }
  
  private Op(OperandFormat f1, OperandFormat f2) {
    this.operands = new OperandFormat[] {f1, f2};
  }
  
  private Op(OperandFormat f1, OperandFormat f2, OperandFormat f3) {
    this.operands = new OperandFormat[] {f1, f2, f3};
  }
  
  private Op(OperandFormat f1, OperandFormat f2, OperandFormat f3, OperandFormat f4) {
    this.operands = new OperandFormat[] {f1, f2, f3, f4};
  }
  
  /**
   * SETLIST sometimes uses an extra byte without tagging it.
   * This means that the value in the extra byte can be detected as any other opcode unless it is recognized.
   */
  public boolean hasExtraByte(int codepoint, CodeExtract ex) {
    if(this == Op.SETLIST) {
      return ex.C.extract(codepoint) == 0;
    } else {
      return false;
    }
  }
  
  public int jumpField(int codepoint, CodeExtract ex) {
    switch(this) {
      case FORPREP54:
      case TFORPREP54:
        return ex.Bx.extract(codepoint);
      case FORLOOP54:
      case TFORLOOP54:
        return -ex.Bx.extract(codepoint);
      case JMP:
      case FORLOOP:
      case FORPREP:
      case JMP52:
      case TFORLOOP52:
      case TFORPREP:
        return ex.sBx.extract(codepoint);
      case JMP54:
        return ex.sJ.extract(codepoint);
      default:
        throw new IllegalStateException();
    }
  }
  
  /**
   * Returns the target register of the instruction at the given
   * line or -1 if the instruction does not have a unique target.
   */
  public int target(int codepoint, CodeExtract ex) {
    switch(this) {
      case MOVE:
      case LOADI: case LOADF: case LOADK: case LOADKX:
      case LOADBOOL: case LOADFALSE: case LFALSESKIP: case LOADTRUE:
      case GETUPVAL:
      case GETTABUP: case GETTABUP54:
      case GETGLOBAL:
      case GETTABLE: case GETTABLE54: case GETI: case GETFIELD:
      case NEWTABLE50: case NEWTABLE: case NEWTABLE54:
      case ADD: case SUB: case MUL: case DIV: case IDIV: case MOD: case POW: case BAND: case BOR: case BXOR: case SHL: case SHR:
      case ADD54: case SUB54: case MUL54: case DIV54: case IDIV54: case MOD54: case POW54: case BAND54: case BOR54: case BXOR54: case SHL54: case SHR54:
      case ADDK: case SUBK: case MULK: case DIVK: case IDIVK: case MODK: case POWK: case BANDK: case BORK: case BXORK:
      case ADDI: case SHLI: case SHRI:
      case UNM: case NOT: case LEN: case BNOT:
      case CONCAT: case CONCAT54:
      case CLOSURE:
      case TEST50: case TESTSET: case TESTSET54:
        return ex.A.extract(codepoint);
      case MMBIN: case MMBINI: case MMBINK:
        return -1; // depends on previous instruction
      case LOADNIL:
        if(ex.A.extract(codepoint) == ex.B.extract(codepoint)) {
          return ex.A.extract(codepoint);
        } else {
          return -1;
        }
      case LOADNIL52:
        if(ex.B.extract(codepoint) == 0) {
          return ex.A.extract(codepoint);
        } else {
          return -1;
        }
      case SETGLOBAL:
      case SETUPVAL:
      case SETTABUP: case SETTABUP54:
      case SETTABLE: case SETTABLE54: case SETI: case SETFIELD:
      case JMP: case JMP52: case JMP54:
      case TAILCALL: case TAILCALL54:
      case RETURN: case RETURN54: case RETURN0: case RETURN1:
      case FORLOOP: case FORLOOP54:
      case FORPREP: case FORPREP54:
      case TFORPREP: case TFORPREP54:
      case TFORCALL: case TFORCALL54:
      case TFORLOOP: case TFORLOOP52: case TFORLOOP54:
      case TBC:
      case CLOSE:
      case EXTRAARG:
      case SELF: case SELF54:
      case EQ: case LT: case LE:
      case EQ54: case LT54: case LE54:
      case EQK: case EQI: case LTI: case LEI: case GTI: case GEI:
      case TEST: case TEST54:
      case SETLIST50: case SETLISTO: case SETLIST: case SETLIST52: case SETLIST54:
      case VARARGPREP:
        return -1;
      case CALL: {
        int a = ex.A.extract(codepoint);
        int c = ex.C.extract(codepoint);
        if(c == 2) {
          return a;
        } else {
          return -1; 
        }
      }
      case VARARG: {
        int a = ex.A.extract(codepoint);
        int b = ex.B.extract(codepoint);
        if(b == 2) {
          return a;
        } else {
          return -1;
        }
      }
      case VARARG54: {
        int a = ex.A.extract(codepoint);
        int c = ex.C.extract(codepoint);
        if(c == 2) {
          return a;
        } else {
          return -1;
        }
      }
      case EXTRABYTE:
        return -1;
    }
    throw new IllegalStateException(this.name());
  }
  
  private String fixedOperand(int field) {
    return Integer.toString(field);
  }
  
  private String registerOperand(int field) {
    return "r" + field;
  }
  
  private String upvalueOperand(int field) {
    return "u" + field;
  }
  
  private String constantOperand(int field) {
    return "k" + field;
  }
  
  private String functionOperand(int field) {
    return "f" + field;
  }
  
  public boolean hasJump() {
    for(int i = 0; i < operands.length; ++i) {
      OperandFormat.Format format = operands[i].format;
      if(format == OperandFormat.Format.JUMP || format == OperandFormat.Format.JUMP_NEGATIVE) {
        return true;
      }
    }
    return false;
  }
  
  public String codePointToString(int codepoint, CodeExtract ex, String label) {
    int width = 10;
    StringBuilder b = new StringBuilder();
    b.append(this.name().toLowerCase());
    for(int i = 0; i < width - name().length(); i++) {
      b.append(' ');
    }
    String[] parameters = new String[operands.length];
    for(int i = 0; i < operands.length; ++i) {
      CodeExtract.Field field;
      switch(operands[i].field) {
      case A: field = ex.A; break;
      case B: field = ex.B; break;
      case C: field = ex.C; break;
      case k: field = ex.k; break;
      case Ax: field = ex.Ax; break;
      case sJ: field = ex.sJ; break;
      case Bx: field = ex.Bx; break;
      case sBx: field = ex.sBx; break;
      case x: field = ex.x; break;
      default: throw new IllegalStateException();
      }
      int x = field.extract(codepoint);
      switch(operands[i].format) {
      case IMMEDIATE_INTEGER:
      case IMMEDIATE_FLOAT:
      case RAW: parameters[i] = fixedOperand(x); break;
      case IMMEDIATE_SIGNED_INTEGER: parameters[i] = fixedOperand(x - field.max() / 2); break;
      case REGISTER: parameters[i] = registerOperand(x); break;
      case UPVALUE: parameters[i] = upvalueOperand(x); break;
      case REGISTER_K:
        if(ex.is_k(x)) {
          parameters[i] = constantOperand(ex.get_k(x));
        } else {
          parameters[i] = registerOperand(x);
        }
        break;
      case REGISTER_K54:
        if(ex.k.extract(codepoint) != 0) {
          parameters[i] = constantOperand(x);
        } else {
          parameters[i] = registerOperand(x);
        }
        break;
      case CONSTANT:
      case CONSTANT_INTEGER:
      case CONSTANT_STRING: parameters[i] = constantOperand(x); break;
      case FUNCTION: parameters[i] = functionOperand(x); break;
      case JUMP:
        if(label != null) {
          parameters[i] = label;
        } else {
          parameters[i] = fixedOperand(x) + operands[i].offset;
        }
        break;
      case JUMP_NEGATIVE:
        if(label != null) {
          parameters[i] = label;
        } else {
          parameters[i] = fixedOperand(-x);
        }
        break;
      default:
        throw new IllegalStateException();
      }
    }
    for(String parameter : parameters) {
      b.append(' ');
      for(int i = 0; i < 5 - parameter.length(); i++) {
        b.append(' ');
      }
      b.append(parameter);
    }
    return b.toString();
  }
}
