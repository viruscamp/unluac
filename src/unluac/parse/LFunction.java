package unluac.parse;

public class LFunction extends BObject {
  
  public BHeader header;
  public LString name;
  public int linedefined;
  public int lastlinedefined;
  public LFunction parent;
  public int[] code;
  public int[] lines;
  public LAbsLineInfo[] abslineinfo;
  public LLocal[] locals;
  public LObject[] constants;
  public LUpvalue[] upvalues;
  public LFunction[] functions;
  public int maximumStackSize;
  public int numUpvalues;
  public int numParams;
  public int vararg;
  public boolean stripped;
  public int level;
  
  public LFunction(BHeader header, LString name, int linedefined, int lastlinedefined, int[] code, int[] lines, LAbsLineInfo[] abslineinfo, LLocal[] locals, LObject[] constants, LUpvalue[] upvalues, LFunction[] functions, int maximumStackSize, int numUpValues, int numParams, int vararg) {
    this.header = header;
    this.name = name;
    this.linedefined = linedefined;
    this.lastlinedefined = lastlinedefined;
    this.code = code;
    this.lines = lines;
    this.abslineinfo = abslineinfo;
    this.locals = locals;
    this.constants = constants;
    this.upvalues = upvalues;
    this.functions = functions;
    this.maximumStackSize = maximumStackSize;
    this.numUpvalues = numUpValues;
    this.numParams = numParams;
    this.vararg = vararg;
    this.stripped = false;
  }
  
  public void setLevel(int level) {
    this.level = level;
    for(LFunction f : functions) {
      f.setLevel(level + 1);
    }
  }
  
}
