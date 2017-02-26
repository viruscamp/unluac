package unluac;

import java.util.HashSet;
import java.util.Set;

import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;
import unluac.parse.LHeaderType;

public abstract class Version {

  public static final Version LUA50 = new Version50();
  public static final Version LUA51 = new Version51();
  public static final Version LUA52 = new Version52();
  public static final Version LUA53 = new Version53();
  
  protected static final Set<String> reservedWords = new HashSet<String>();
  
  static {
    reservedWords.add("and");
    reservedWords.add("break");
    reservedWords.add("do");
    reservedWords.add("else");
    reservedWords.add("elseif");
    reservedWords.add("end");
    reservedWords.add("false");
    reservedWords.add("for");
    reservedWords.add("function");
    reservedWords.add("if");
    reservedWords.add("in");
    reservedWords.add("local");
    reservedWords.add("nil");
    reservedWords.add("not");
    reservedWords.add("or");
    reservedWords.add("repeat");
    reservedWords.add("return");
    reservedWords.add("then");
    reservedWords.add("true");
    reservedWords.add("until");
    reservedWords.add("while");
  }
  
  public static enum VarArgType {
    ARG,
    HYBRID,
    ELLIPSIS;
  }
  
  protected final int versionNumber;
  
  protected Version(int versionNumber) {
    this.versionNumber = versionNumber;
  }
  
  public abstract LHeaderType getLHeaderType();
  
  public OpcodeMap getOpcodeMap() {
    return new OpcodeMap(versionNumber);
  }
  
  public abstract int getOuterBlockScopeAdjustment();

  public abstract boolean usesInlineUpvalueDeclarations();
  
  public abstract Op getTForTarget();

  public abstract Op getForTarget();
  
  public abstract boolean isAllowedPreceedingSemicolon();
  
  public abstract boolean isEnvironmentTable(String name);
  
  public abstract boolean usesIfBreakRewrite();
  
  public abstract VarArgType getVarArgType();
  
  public abstract boolean isReserved(String word);
  
  public int getConstantsOffset() {
    return 256;
  }
  
}

class Version50 extends Version {

  Version50() {
    super(0x50);
  }

  @Override
  public LHeaderType getLHeaderType() {
    return LHeaderType.TYPE50;
  }

  @Override
  public int getOuterBlockScopeAdjustment() {
    return -1;
  }

  @Override
  public boolean usesInlineUpvalueDeclarations() {
    return true;
  }

  @Override
  public Op getTForTarget() {
    return null;
  }

  @Override
  public Op getForTarget() {
    return Op.FORLOOP;
  }

  @Override
  public boolean isAllowedPreceedingSemicolon() {
    return false;
  }
  
  @Override
  public boolean isEnvironmentTable(String upvalue) {
    return false;
  }
  
  @Override
  public boolean usesIfBreakRewrite() {
    return false;
  }
  
  @Override
  public VarArgType getVarArgType() {
    return VarArgType.ARG;
  }
  
  @Override
  public boolean isReserved(String word) {
    return reservedWords.contains(word);
  }
  
  @Override
  public int getConstantsOffset() {
    return 250;
  }
  
}

class Version51 extends Version {
  
  Version51() {
    super(0x51);
  }
  
  @Override
  public LHeaderType getLHeaderType() {
    return LHeaderType.TYPE51;
  }
  
  @Override
  public int getOuterBlockScopeAdjustment() {
    return -1;
  }
  
  @Override
  public boolean usesInlineUpvalueDeclarations() {
    return true;
  }
  
  @Override
  public Op getTForTarget() {
    return Op.TFORLOOP;
  }
  
  @Override
  public Op getForTarget() {
    return null;
  }

  @Override
  public boolean isAllowedPreceedingSemicolon() {
    return false;
  }
  
  @Override
  public boolean isEnvironmentTable(String upvalue) {
    return false;
  }
  
  @Override
  public boolean usesIfBreakRewrite() {
    return false;
  }
  
  @Override
  public VarArgType getVarArgType() {
    return VarArgType.HYBRID;
  }
  
  @Override
  public boolean isReserved(String word) {
    return reservedWords.contains(word);
  }
  
}

class Version52 extends Version {
  
  Version52() {
    super(0x52);
  }
  
  @Override
  public LHeaderType getLHeaderType() {
    return LHeaderType.TYPE52;
  }
  
  @Override
  public int getOuterBlockScopeAdjustment() {
    return 0;
  }
  
  @Override
  public boolean usesInlineUpvalueDeclarations() {
    return false;
  }

  @Override
  public Op getTForTarget() {
    return Op.TFORCALL;
  }

  @Override
  public Op getForTarget() {
    return null;
  }

  @Override
  public boolean isAllowedPreceedingSemicolon() {
    return true;
  }
  
  @Override
  public boolean isEnvironmentTable(String name) {
    return name.equals("_ENV");
  }
  
  @Override
  public boolean usesIfBreakRewrite() {
    return true;
  }
  
  @Override
  public VarArgType getVarArgType() {
    return VarArgType.ELLIPSIS;
  }
  
  @Override
  public boolean isReserved(String word) {
    return reservedWords.contains(word) || word.equals("goto");
  }
  
}

class Version53 extends Version {
  
  Version53() {
    super(0x53);
  }
  
  @Override
  public LHeaderType getLHeaderType() {
    return LHeaderType.TYPE53;
  }
  
  @Override
  public int getOuterBlockScopeAdjustment() {
    return 0;
  }
  
  @Override
  public boolean usesInlineUpvalueDeclarations() {
    return false;
  }

  @Override
  public Op getTForTarget() {
    return Op.TFORCALL;
  }

  @Override
  public Op getForTarget() {
    return null;
  }

  @Override
  public boolean isAllowedPreceedingSemicolon() {
    return true;
  }
  
  @Override
  public boolean isEnvironmentTable(String name) {
    return name.equals("_ENV");
  }
  
  @Override
  public boolean usesIfBreakRewrite() {
    return true;
  }
  
  @Override
  public VarArgType getVarArgType() {
    return VarArgType.ELLIPSIS;
  }
  
  @Override
  public boolean isReserved(String word) {
    return reservedWords.contains(word) || word.equals("goto");
  }
  
}

