package unluac;

import java.util.HashSet;
import java.util.Set;

import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;
import unluac.parse.LConstantType;
import unluac.parse.LFunctionType;
import unluac.parse.LHeaderType;
import unluac.parse.LStringType;
import unluac.parse.LUpvalueType;

public class Version {

  public static Version getVersion(int major, int minor) {
    return new Version(major, minor);
  }
  
  public static class Setting<T> {
    
    private final T value;
    
    public Setting(T value) {
      this.value = value;
    }
    
    public T get() {
      return value;
    }
    
  }
  
  public static enum VarArgType {
    ARG,
    HYBRID,
    ELLIPSIS,
  }
  
  public static enum HeaderType {
    LUA50,
    LUA51,
    LUA52,
    LUA53,
    LUA54,
  }
  
  public static enum StringType {
    LUA50,
    LUA53,
    LUA54,
  }
  
  public static enum ConstantType {
    LUA50,
    LUA53,
    LUA54,
  }
  
  public static enum UpvalueType {
    LUA50,
    LUA54
  }
  
  public static enum FunctionType {
    LUA50,
    LUA51,
    LUA52,
    LUA53,
    LUA54,
  }
  
  public static enum OpcodeMapType {
    LUA50,
    LUA51,
    LUA52,
    LUA53,
    LUA54,
  }
  
  public static enum UpvalueDeclarationType {
    INLINE,
    HEADER,
  }
  
  public static enum InstructionFormat {
    LUA50,
    LUA51,
    LUA54,
  }
  
  public static enum WhileFormat {
    TOP_CONDITION,
    BOTTOM_CONDITION,
  }
  
  public final Setting<VarArgType> varargtype;
  public final Setting<Boolean> useupvaluecountinheader;
  public final Setting<InstructionFormat> instructionformat;
  public final Setting<Integer> outerblockscopeadjustment;
  public final Setting<Boolean> closeinscope;
  public final Setting<UpvalueDeclarationType> upvaluedeclarationtype;
  public final Setting<Op> fortarget;
  public final Setting<Op> tfortarget;
  public final Setting<WhileFormat> whileformat;
  public final Setting<Boolean> allowpreceedingsemicolon;
  public final Setting<Boolean> usenestinglongstrings;
  public final Setting<String> environmenttable;
  public final Setting<Boolean> useifbreakrewrite;
  public final Setting<Boolean> usegoto;
  public final Setting<Integer> rkoffset;
  
  private final int major;
  private final int minor;
  private final String name;
  private final Set<String> reservedWords;
  private final LHeaderType lheadertype;
  private final LStringType lstringtype;
  private final LConstantType lconstanttype;
  private final LUpvalueType lupvaluetype;
  private final LFunctionType lfunctiontype;
  private final OpcodeMap opcodemap;
  
  private Version(int major, int minor) {
    HeaderType headertype;
    StringType stringtype;
    ConstantType constanttype;
    UpvalueType upvaluetype;
    FunctionType functiontype;
    OpcodeMapType opcodemap;
    this.major = major;
    this.minor = minor;
    name = major + "." + minor;
    if(major == 5 && minor >= 0 && minor <= 4) {
      switch(minor) {
        case 0:
          varargtype = new Setting<>(VarArgType.ARG);
          useupvaluecountinheader = new Setting<>(false);
          headertype = HeaderType.LUA50;
          stringtype = StringType.LUA50;
          constanttype = ConstantType.LUA50;
          upvaluetype = UpvalueType.LUA50;
          functiontype = FunctionType.LUA50;
          opcodemap = OpcodeMapType.LUA50;
          instructionformat = new Setting<>(InstructionFormat.LUA50);
          outerblockscopeadjustment = new Setting<>(-1);
          closeinscope = new Setting<Boolean>(true);
          upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.INLINE);
          fortarget = new Setting<>(Op.FORLOOP);
          tfortarget = new Setting<>(null);
          whileformat = new Setting<>(WhileFormat.BOTTOM_CONDITION);
          allowpreceedingsemicolon = new Setting<>(false);
          usenestinglongstrings = new Setting<>(true);
          environmenttable = new Setting<>(null);
          useifbreakrewrite = new Setting<>(false);
          usegoto = new Setting<>(false);
          rkoffset = new Setting<>(250);
          break;
        case 1:
          varargtype = new Setting<>(VarArgType.HYBRID);
          useupvaluecountinheader = new Setting<>(false);
          headertype = HeaderType.LUA51;
          stringtype = StringType.LUA50;
          constanttype = ConstantType.LUA50;
          upvaluetype = UpvalueType.LUA50;
          functiontype = FunctionType.LUA51;
          opcodemap = OpcodeMapType.LUA51;
          instructionformat = new Setting<>(InstructionFormat.LUA51);
          outerblockscopeadjustment = new Setting<>(-1);
          closeinscope = new Setting<Boolean>(true);
          upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.INLINE);
          fortarget = new Setting<>(null);
          tfortarget = new Setting<>(Op.TFORLOOP);
          whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
          allowpreceedingsemicolon = new Setting<>(false);
          usenestinglongstrings = new Setting<>(false);
          environmenttable = new Setting<>(null);
          useifbreakrewrite = new Setting<>(false);
          usegoto = new Setting<>(false);
          rkoffset = new Setting<>(256);
          break;
        case 2:
          varargtype = new Setting<>(VarArgType.ELLIPSIS);
          useupvaluecountinheader = new Setting<>(false);
          headertype = HeaderType.LUA52;
          stringtype = StringType.LUA50;
          constanttype = ConstantType.LUA50;
          upvaluetype = UpvalueType.LUA50;
          functiontype = FunctionType.LUA52;
          opcodemap = OpcodeMapType.LUA52;
          instructionformat = new Setting<>(InstructionFormat.LUA51);
          outerblockscopeadjustment = new Setting<>(0);
          closeinscope = new Setting<Boolean>(null);
          upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.HEADER);
          fortarget = new Setting<>(null);
          tfortarget = new Setting<>(Op.TFORCALL);
          whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
          allowpreceedingsemicolon = new Setting<>(true);
          usenestinglongstrings = new Setting<>(false);
          environmenttable = new Setting<>("_ENV");
          useifbreakrewrite = new Setting<>(true);
          usegoto = new Setting<>(true);
          rkoffset = new Setting<>(256);
          break;
        case 3:
          varargtype = new Setting<>(VarArgType.ELLIPSIS);
          useupvaluecountinheader = new Setting<>(true);
          headertype = HeaderType.LUA53;
          stringtype = StringType.LUA53;
          constanttype = ConstantType.LUA53;
          upvaluetype = UpvalueType.LUA50;
          functiontype = FunctionType.LUA53;
          opcodemap = OpcodeMapType.LUA53;
          instructionformat = new Setting<>(InstructionFormat.LUA51);
          outerblockscopeadjustment = new Setting<>(0);
          closeinscope = new Setting<Boolean>(null);
          upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.HEADER);
          fortarget = new Setting<>(null);
          tfortarget = new Setting<>(Op.TFORCALL);
          whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
          allowpreceedingsemicolon = new Setting<>(true);
          usenestinglongstrings = new Setting<>(false);
          environmenttable = new Setting<>("_ENV");
          useifbreakrewrite = new Setting<>(true);
          usegoto = new Setting<>(true);
          rkoffset = new Setting<>(256);
          break;
        case 4:
          varargtype = new Setting<>(VarArgType.ELLIPSIS);
          useupvaluecountinheader = new Setting<>(true);
          headertype = HeaderType.LUA54;
          stringtype = StringType.LUA54;
          constanttype = ConstantType.LUA54;
          upvaluetype = UpvalueType.LUA54;
          functiontype = FunctionType.LUA54;
          opcodemap = OpcodeMapType.LUA54;
          instructionformat = new Setting<>(InstructionFormat.LUA54);
          outerblockscopeadjustment = new Setting<>(0);
          closeinscope = new Setting<Boolean>(false);
          upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.HEADER);
          fortarget = new Setting<>(null);
          tfortarget = new Setting<>(null);
          whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
          allowpreceedingsemicolon = new Setting<>(true);
          usenestinglongstrings = new Setting<>(false);
          environmenttable = new Setting<>("_ENV");
          useifbreakrewrite = new Setting<>(true);
          usegoto = new Setting<>(true);
          rkoffset = new Setting<>(null);
          break;
        default: throw new IllegalStateException();
      }
    } else {
      throw new IllegalStateException();
    }
    
    reservedWords = new HashSet<String>();
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
    if(usegoto.get()) {
      reservedWords.add("goto");
    }
    
    this.lheadertype = LHeaderType.get(headertype);
    this.lstringtype = LStringType.get(stringtype);
    this.lconstanttype = LConstantType.get(constanttype);
    this.lupvaluetype = LUpvalueType.get(upvaluetype);
    this.lfunctiontype = LFunctionType.get(functiontype);
    this.opcodemap = new OpcodeMap(opcodemap);
  }
  
  public int getVersionMajor() {
    return major;
  }
  
  public int getVersionMinor() {
    return minor;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean isEnvironmentTable(String name) {
    String env = environmenttable.get();
    if(env != null) {
      return name.equals(env);
    } else {
      return false;
    }
  }
  
  public boolean isReserved(String name) {
    return reservedWords.contains(name);
  }
  
  public LHeaderType getLHeaderType() {
    return lheadertype;
  }
  
  public LStringType getLStringType() {
    return lstringtype;
  }
  
  public LConstantType getLConstantType() {
    return lconstanttype;
  }
  
  public LUpvalueType getLUpvalueType() {
    return lupvaluetype;
  }
  
  public LFunctionType getLFunctionType() {
    return lfunctiontype;
  }
  
  public OpcodeMap getOpcodeMap() {
    return opcodemap;
  }
  
}
