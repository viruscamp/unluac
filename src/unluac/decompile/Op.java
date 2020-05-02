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
  // Special
  EXTRABYTE(OperandFormat.x);
  
  public final OperandFormat[] operands;
  
  private Op(OperandFormat f1) {
    this.operands = new OperandFormat[] {f1};
  }
  
  private Op(OperandFormat f1, OperandFormat f2) {
    this.operands = new OperandFormat[] {f1, f2};
  }
  
  private Op(OperandFormat f1, OperandFormat f2, OperandFormat f3) {
    this.operands = new OperandFormat[] {f1, f2, f3};
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
        return ex.A.extract(codepoint);
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
      case SETTABUP:
      case SETTABLE:
      case JMP:
      case JMP52:
      case TAILCALL:
      case RETURN:
      case FORLOOP:
      case FORPREP:
      case TFORPREP:
      case TFORCALL:
      case TFORLOOP:
      case TFORLOOP52:
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
      case EXTRABYTE:
        return -1;
    }
    throw new IllegalStateException();
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
  
  private String rkOperand(int field, CodeExtract ex) {
    if(ex.is_k(field)) {
      return constantOperand(ex.get_k(field));
    } else {
      return registerOperand(field);
    }
  }
    
  public boolean hasJump() {
    for(int i = 0; i < operands.length; ++i) {
      if(operands[i] == OperandFormat.sBxJ) {
        return true;
      }
    }
    return false;
  }
  
  public int getJumpOffset(int codepoint, CodeExtract ex) {
    return ex.sBx.extract(codepoint);
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
      switch(operands[i]) {
      case A:
        parameters[i] = fixedOperand(ex.A.extract(codepoint));
        break;
      case AR:
        parameters[i] = registerOperand(ex.A.extract(codepoint));
        break;
      case AU:
        parameters[i] = upvalueOperand(ex.A.extract(codepoint));
        break;
      case B:
        parameters[i] = fixedOperand(ex.B.extract(codepoint));
        break;
      case BR:
        parameters[i] = registerOperand(ex.B.extract(codepoint));
        break;
      case BRK:
        parameters[i] = rkOperand(ex.B.extract(codepoint), ex);
        break;
      case BU:
        parameters[i] = upvalueOperand(ex.B.extract(codepoint));
        break;
      case C:
        parameters[i] = fixedOperand(ex.C.extract(codepoint));
        break;
      case CR:
        parameters[i] = registerOperand(ex.C.extract(codepoint));
        break;
      case CRK:
        parameters[i] = rkOperand(ex.C.extract(codepoint), ex);
        break;
      case Ax:
        parameters[i] = fixedOperand(ex.Ax.extract(codepoint));
        break;
      case Bx:
        parameters[i] = fixedOperand(ex.Bx.extract(codepoint));
        break;
      case BxK:
        parameters[i] = constantOperand(ex.Bx.extract(codepoint));
        break;
      case BxF:
        parameters[i] = functionOperand(ex.Bx.extract(codepoint));
        break;
      case sBxJ:
        if(label != null) {
          parameters[i] = label;
        } else {
          parameters[i] = fixedOperand(ex.sBx.extract(codepoint));
        }
        break;
      case x:
        parameters[i] = fixedOperand(codepoint);
        break;
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
