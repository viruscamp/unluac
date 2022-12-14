package unluac.decompile;

import unluac.Version;

class OpV {
  public static final int LUA50 = 1;
  public static final int LUA51 = 2;
  public static final int LUA52 = 4;
  public static final int LUA53 = 8;
  public static final int LUA54 = 16;
}

public enum Op {
  // Lua 5.1 Opcodes
  MOVE("move", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.BR),
  LOADK("loadk", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.BxK),
  LOADBOOL("loadbool", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  LOADNIL("loadnil", OpV.LUA50 | OpV.LUA51, OperandFormat.AR, OperandFormat.BR),
  GETUPVAL("getupval", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.BU),
  GETGLOBAL("getglobal", OpV.LUA50 | OpV.LUA51, OperandFormat.AR, OperandFormat.BxK),
  GETTABLE("gettable", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK),
  SETGLOBAL("setglobal", OpV.LUA50 | OpV.LUA51, OperandFormat.AR, OperandFormat.BxK),
  SETUPVAL("setupval", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.BU),
  SETTABLE("settable", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  NEWTABLE("newtable", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  SELF("self", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK),
  ADD("add", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  SUB("sub", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  MUL("mul", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  DIV("div", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  MOD("mod", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  POW("pow", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  UNM("unm", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BR),
  NOT("not", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BR),
  LEN("len", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BR),
  CONCAT("concat", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  JMP("jmp", OpV.LUA50 | OpV.LUA51, OperandFormat.sBxJ),
  EQ("eq", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
  LT("lt", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
  LE("le", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
  TEST("test", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.C),
  TESTSET("testset", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
  CALL("call", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  TAILCALL("tailcall", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.B),
  RETURN("return", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.B),
  FORLOOP("forloop", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.sBxJ),
  FORPREP("forprep", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.sBxJ),
  TFORLOOP("tforloop", OpV.LUA50 | OpV.LUA51, OperandFormat.AR, OperandFormat.C),
  SETLIST("setlist", OpV.LUA51, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  CLOSE("close", OpV.LUA50 | OpV.LUA51 | OpV.LUA54, OperandFormat.AR),
  CLOSURE("closure", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.BxF),
  VARARG("vararg", OpV.LUA51 | OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.B),
  // Lua 5.2 Opcodes
  JMP52("jmp", OpV.LUA52 | OpV.LUA53, OperandFormat.A, OperandFormat.sBxJ),
  LOADNIL52("loadnil", OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.B),
  LOADKX("loadkx", OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.AR),
  GETTABUP("gettabup", OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.BU, OperandFormat.CRK),
  SETTABUP("settabup", OpV.LUA52 | OpV.LUA53, OperandFormat.AU, OperandFormat.BRK, OperandFormat.CRK),
  SETLIST52("setlist", OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  TFORCALL("tforcall", OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.C),
  TFORLOOP52("tforloop", OpV.LUA52 | OpV.LUA53, OperandFormat.AR, OperandFormat.sBxJ),
  EXTRAARG("extraarg", OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.Ax),
  // Lua 5.0 Opcodes
  NEWTABLE50("newtable", OpV.LUA50, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
  SETLIST50("setlist", OpV.LUA50, OperandFormat.AR, OperandFormat.Bx),
  SETLISTO("setlisto", OpV.LUA50, OperandFormat.AR, OperandFormat.Bx),
  TFORPREP("tforprep", OpV.LUA50, OperandFormat.AR, OperandFormat.sBxJ),
  TEST50("test", OpV.LUA50, OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
  // Lua 5.3 Opcodes
  IDIV("idiv", OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BAND("band", OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BOR("bor", OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BXOR("bxor", OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  SHL("shl", OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  SHR("shr", OpV.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  BNOT("bnot", OpV.LUA53 | OpV.LUA54, OperandFormat.AR, OperandFormat.BR),
  // Lua 5.4 Opcodes
  LOADI("loadi", OpV.LUA54, OperandFormat.AR, OperandFormat.sBxI),
  LOADF("loadf", OpV.LUA54, OperandFormat.AR, OperandFormat.sBxF),
  LOADFALSE("loadfalse", OpV.LUA54, OperandFormat.AR),
  LFALSESKIP("lfalseskip", OpV.LUA54, OperandFormat.AR),
  LOADTRUE("loadtrue", OpV.LUA54, OperandFormat.AR),
  GETTABUP54("gettabup", OpV.LUA54, OperandFormat.AR, OperandFormat.BU, OperandFormat.CKS),
  GETTABLE54("gettable", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  GETI("geti", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CI),
  GETFIELD("getfield", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKS),
  SETTABUP54("settabup", OpV.LUA54, OperandFormat.AU, OperandFormat.BK, OperandFormat.CRK54),
  SETTABLE54("settable", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK54),
  SETI("seti", OpV.LUA54, OperandFormat.AR, OperandFormat.BI, OperandFormat.CRK54),
  SETFIELD("setfield", OpV.LUA54, OperandFormat.AR, OperandFormat.BKS, OperandFormat.CRK54),
  NEWTABLE54("newtable", OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  SELF54("self", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK54),
  ADDI("addi", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CsI),
  ADDK("addk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  SUBK("subk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  MULK("mulk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  MODK("modk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  POWK("powk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  DIVK("divk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  IDIVK("idivk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
  BANDK("bandk", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
  BORK("bork", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
  BXORK("bxork", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
  SHRI("shri", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CsI),
  SHLI("shli", OpV.LUA54, OperandFormat.AR, OperandFormat.CsI, OperandFormat.BR),
  ADD54("add", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  SUB54("sub", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  MUL54("mul", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  MOD54("mod", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  POW54("pow", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  DIV54("div", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  IDIV54("idiv", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  BAND54("band", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  BOR54("bor", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  BXOR54("bxor", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  SHL54("shl", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  SHR54("shr", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
  MMBIN("mmbin", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
  MMBINI("mmbini", OpV.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.C, OperandFormat.k),
  MMBINK("mmbink", OpV.LUA54, OperandFormat.AR, OperandFormat.BK, OperandFormat.C, OperandFormat.k),
  CONCAT54("concat", OpV.LUA54, OperandFormat.AR, OperandFormat.B),
  TBC("tbc", OpV.LUA54, OperandFormat.AR),
  JMP54("jmp", OpV.LUA54, OperandFormat.sJ),
  EQ54("eq", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k, OperandFormat.C),
  LT54("lt", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k, OperandFormat.C),
  LE54("le", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k, OperandFormat.C),
  EQK("eqk", OpV.LUA54, OperandFormat.AR, OperandFormat.BK, OperandFormat.k, OperandFormat.C),
  EQI("eqi", OpV.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  LTI("lti", OpV.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  LEI("lei", OpV.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  GTI("gti", OpV.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  GEI("gei", OpV.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
  TEST54("test", OpV.LUA54, OperandFormat.AR, OperandFormat.k),
  TESTSET54("testset", OpV.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k),
  TAILCALL54("tailcall", OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  RETURN54("return", OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  RETURN0("return0", OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  RETURN1("return1", OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  FORLOOP54("forloop", OpV.LUA54, OperandFormat.AR, OperandFormat.BxJn),
  FORPREP54("forprep", OpV.LUA54, OperandFormat.AR, OperandFormat.BxJ),
  TFORPREP54("tforprep", OpV.LUA54, OperandFormat.AR, OperandFormat.BxJ),
  TFORCALL54("tforcall", OpV.LUA54, OperandFormat.AR, OperandFormat.C),
  TFORLOOP54("tforloop", OpV.LUA54, OperandFormat.AR, OperandFormat.BxJn),
  SETLIST54("setlist", OpV.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
  VARARG54("vararg", OpV.LUA54, OperandFormat.AR, OperandFormat.C),
  VARARGPREP("varargprep", OpV.LUA54, OperandFormat.A),
  // Special
  EXTRABYTE("extrabyte", OpV.LUA50 | OpV.LUA51 | OpV.LUA52 | OpV.LUA53 | OpV.LUA54, OperandFormat.x),
  DEFAULT("default", 0, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
  DEFAULT54("default", 0, OperandFormat.AR, OperandFormat.BR, OperandFormat.C, OperandFormat.k);
  
  public final String name;
  public final int versions;
  public final OperandFormat[] operands;
  
  private Op(String name, int versions) {
    this.name = name;
    this.versions = versions;
    this.operands = new OperandFormat[] {};
  }
  
  private Op(String name, int versions, OperandFormat f1) {
    this.name = name;
    this.versions = versions;
    this.operands = new OperandFormat[] {f1};
  }
  
  private Op(String name, int versions, OperandFormat f1, OperandFormat f2) {
    this.name = name;
    this.versions = versions;
    this.operands = new OperandFormat[] {f1, f2};
  }
  
  private Op(String name, int versions, OperandFormat f1, OperandFormat f2, OperandFormat f3) {
    this.name = name;
    this.versions = versions;
    this.operands = new OperandFormat[] {f1, f2, f3};
  }
  
  private Op(String name, int versions, OperandFormat f1, OperandFormat f2, OperandFormat f3, OperandFormat f4) {
    this.name = name;
    this.versions = versions;
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
      case DEFAULT:
      case DEFAULT54:
        throw new IllegalStateException();
    }
    throw new IllegalStateException(this.name());
  }
  
  private static String fixedOperand(int field) {
    return Integer.toString(field);
  }
  
  private static String registerOperand(int field) {
    return "r" + field;
  }
  
  private static String upvalueOperand(int field) {
    return "u" + field;
  }
  
  private static String constantOperand(int field) {
    return "k" + field;
  }
  
  private static String functionOperand(int field) {
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
    return toStringHelper(name, operands, codepoint, ex, label);
  }
  
  public static String defaultToString(int codepoint, Version version, CodeExtract ex) {
    return toStringHelper(String.format("op%02d", ex.op.extract(codepoint)), version.getDefaultOp().operands, codepoint, ex, null);
  }
  
  private static String toStringHelper(String name, OperandFormat[] operands, int codepoint, CodeExtract ex, String label) {
    int width = 10;
    StringBuilder b = new StringBuilder();
    b.append(name);
    for(int i = 0; i < width - name.length(); i++) {
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
          parameters[i] = fixedOperand(x + operands[i].offset);
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
