package unluac.decompile;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unluac.Version;

public class OpcodeMap {

  private Op[] map;
  private Map<String, Op> lookup;
  
  public OpcodeMap(Map<Integer, Op> useropmap) {
    int max = -1;
    for(int opcode : useropmap.keySet()) {
      max = Math.max(opcode, max);
    }
    map = new Op[max + 1];
    for(Entry<Integer, Op> entry : useropmap.entrySet()) {
      map[entry.getKey()] = entry.getValue();
    }
    init_lookup();
    setup_lookup(false);
  }
  
  public OpcodeMap(Version.OpcodeMapType type) {
    init_lookup();
    switch(type) {
      case LUA50:
        map = new Op[35];
        map[0] = Op.MOVE;
        map[1] = Op.LOADK;
        map[2] = Op.LOADBOOL;
        map[3] = Op.LOADNIL;
        map[4] = Op.GETUPVAL;
        map[5] = Op.GETGLOBAL;
        map[6] = Op.GETTABLE;
        map[7] = Op.SETGLOBAL;
        map[8] = Op.SETUPVAL;
        map[9] = Op.SETTABLE;
        map[10] = Op.NEWTABLE50;
        map[11] = Op.SELF;
        map[12] = Op.ADD;
        map[13] = Op.SUB;
        map[14] = Op.MUL;
        map[15] = Op.DIV;
        map[16] = Op.POW;
        map[17] = Op.UNM;
        map[18] = Op.NOT;
        map[19] = Op.CONCAT;
        map[20] = Op.JMP;
        map[21] = Op.EQ;
        map[22] = Op.LT;
        map[23] = Op.LE;
        map[24] = Op.TEST50;
        map[25] = Op.CALL;
        map[26] = Op.TAILCALL;
        map[27] = Op.RETURN;
        map[28] = Op.FORLOOP;
        map[29] = Op.TFORLOOP;
        map[30] = Op.TFORPREP;
        map[31] = Op.SETLIST50;
        map[32] = Op.SETLISTO;
        map[33] = Op.CLOSE;
        map[34] = Op.CLOSURE;
        allow_51_math_lookup();
        allow_53_math_lookup();
        break;
      case LUA51:
        map = new Op[38];
        map[0] = Op.MOVE;
        map[1] = Op.LOADK;
        map[2] = Op.LOADBOOL;
        map[3] = Op.LOADNIL;
        map[4] = Op.GETUPVAL;
        map[5] = Op.GETGLOBAL;
        map[6] = Op.GETTABLE;
        map[7] = Op.SETGLOBAL;
        map[8] = Op.SETUPVAL;
        map[9] = Op.SETTABLE;
        map[10] = Op.NEWTABLE;
        map[11] = Op.SELF;
        map[12] = Op.ADD;
        map[13] = Op.SUB;
        map[14] = Op.MUL;
        map[15] = Op.DIV;
        map[16] = Op.MOD;
        map[17] = Op.POW;
        map[18] = Op.UNM;
        map[19] = Op.NOT;
        map[20] = Op.LEN;
        map[21] = Op.CONCAT;
        map[22] = Op.JMP;
        map[23] = Op.EQ;
        map[24] = Op.LT;
        map[25] = Op.LE;
        map[26] = Op.TEST;
        map[27] = Op.TESTSET;
        map[28] = Op.CALL;
        map[29] = Op.TAILCALL;
        map[30] = Op.RETURN;
        map[31] = Op.FORLOOP;
        map[32] = Op.FORPREP;
        map[33] = Op.TFORLOOP;
        map[34] = Op.SETLIST;
        map[35] = Op.CLOSE;
        map[36] = Op.CLOSURE;
        map[37] = Op.VARARG;
        allow_53_math_lookup();
        break;
      case LUA52:
        map = new Op[40];
        map[0] = Op.MOVE;
        map[1] = Op.LOADK;
        map[2] = Op.LOADKX;
        map[3] = Op.LOADBOOL;
        map[4] = Op.LOADNIL52;
        map[5] = Op.GETUPVAL;
        map[6] = Op.GETTABUP;
        map[7] = Op.GETTABLE;
        map[8] = Op.SETTABUP;
        map[9] = Op.SETUPVAL;
        map[10] = Op.SETTABLE;
        map[11] = Op.NEWTABLE;
        map[12] = Op.SELF;
        map[13] = Op.ADD;
        map[14] = Op.SUB;
        map[15] = Op.MUL;
        map[16] = Op.DIV;
        map[17] = Op.MOD;
        map[18] = Op.POW;
        map[19] = Op.UNM;
        map[20] = Op.NOT;
        map[21] = Op.LEN;
        map[22] = Op.CONCAT;
        map[23] = Op.JMP52;
        map[24] = Op.EQ;
        map[25] = Op.LT;
        map[26] = Op.LE;
        map[27] = Op.TEST;
        map[28] = Op.TESTSET;
        map[29] = Op.CALL;
        map[30] = Op.TAILCALL;
        map[31] = Op.RETURN;
        map[32] = Op.FORLOOP;
        map[33] = Op.FORPREP;
        map[34] = Op.TFORCALL;
        map[35] = Op.TFORLOOP52;
        map[36] = Op.SETLIST52;
        map[37] = Op.CLOSURE;
        map[38] = Op.VARARG;
        map[39] = Op.EXTRAARG;
        allow_53_math_lookup();
        break;
      case LUA53:
        map = new Op[47];
        map[0] = Op.MOVE;
        map[1] = Op.LOADK;
        map[2] = Op.LOADKX;
        map[3] = Op.LOADBOOL;
        map[4] = Op.LOADNIL52;
        map[5] = Op.GETUPVAL;
        map[6] = Op.GETTABUP;
        map[7] = Op.GETTABLE;
        map[8] = Op.SETTABUP;
        map[9] = Op.SETUPVAL;
        map[10] = Op.SETTABLE;
        map[11] = Op.NEWTABLE;
        map[12] = Op.SELF;
        map[13] = Op.ADD;
        map[14] = Op.SUB;
        map[15] = Op.MUL;
        map[16] = Op.MOD;
        map[17] = Op.POW;
        map[18] = Op.DIV;
        map[19] = Op.IDIV;
        map[20] = Op.BAND;
        map[21] = Op.BOR;
        map[22] = Op.BXOR;
        map[23] = Op.SHL;
        map[24] = Op.SHR;
        map[25] = Op.UNM;
        map[26] = Op.BNOT;
        map[27] = Op.NOT;
        map[28] = Op.LEN;
        map[29] = Op.CONCAT;
        map[30] = Op.JMP52;
        map[31] = Op.EQ;
        map[32] = Op.LT;
        map[33] = Op.LE;
        map[34] = Op.TEST;
        map[35] = Op.TESTSET;
        map[36] = Op.CALL;
        map[37] = Op.TAILCALL;
        map[38] = Op.RETURN;
        map[39] = Op.FORLOOP;
        map[40] = Op.FORPREP;
        map[41] = Op.TFORCALL;
        map[42] = Op.TFORLOOP52;
        map[43] = Op.SETLIST52;
        map[44] = Op.CLOSURE;
        map[45] = Op.VARARG;
        map[46] = Op.EXTRAARG;
        break;
      case LUA54:
        map = new Op[83];
        map[0] = Op.MOVE;
        map[1] = Op.LOADI;
        map[2] = Op.LOADF;
        map[3] = Op.LOADK;
        map[4] = Op.LOADKX;
        map[5] = Op.LOADFALSE;
        map[6] = Op.LFALSESKIP;
        map[7] = Op.LOADTRUE;
        map[8] = Op.LOADNIL52;
        map[9] = Op.GETUPVAL;
        map[10] = Op.SETUPVAL;
        map[11] = Op.GETTABUP54;
        map[12] = Op.GETTABLE54;
        map[13] = Op.GETI;
        map[14] = Op.GETFIELD;
        map[15] = Op.SETTABUP54;
        map[16] = Op.SETTABLE54;
        map[17] = Op.SETI;
        map[18] = Op.SETFIELD;
        map[19] = Op.NEWTABLE54;
        map[20] = Op.SELF54;
        map[21] = Op.ADDI;
        map[22] = Op.ADDK;
        map[23] = Op.SUBK;
        map[24] = Op.MULK;
        map[25] = Op.MODK;
        map[26] = Op.POWK;
        map[27] = Op.DIVK;
        map[28] = Op.IDIVK;
        map[29] = Op.BANDK;
        map[30] = Op.BORK;
        map[31] = Op.BXORK;
        map[32] = Op.SHRI;
        map[33] = Op.SHLI;
        map[34] = Op.ADD54;
        map[35] = Op.SUB54;
        map[36] = Op.MUL54;
        map[37] = Op.MOD54;
        map[38] = Op.POW54;
        map[39] = Op.DIV54;
        map[40] = Op.IDIV54;
        map[41] = Op.BAND54;
        map[42] = Op.BOR54;
        map[43] = Op.BXOR54;
        map[44] = Op.SHL54;
        map[45] = Op.SHR54;
        map[46] = Op.MMBIN;
        map[47] = Op.MMBINI;
        map[48] = Op.MMBINK;
        map[49] = Op.UNM;
        map[50] = Op.BNOT;
        map[51] = Op.NOT;
        map[52] = Op.LEN;
        map[53] = Op.CONCAT54;
        map[54] = Op.CLOSE;
        map[55] = Op.TBC;
        map[56] = Op.JMP54;
        map[57] = Op.EQ54;
        map[58] = Op.LT54;
        map[59] = Op.LE54;
        map[60] = Op.EQK;
        map[61] = Op.EQI;
        map[62] = Op.LTI;
        map[63] = Op.LEI;
        map[64] = Op.GTI;
        map[65] = Op.GEI;
        map[66] = Op.TEST54;
        map[67] = Op.TESTSET54;
        map[68] = Op.CALL;
        map[69] = Op.TAILCALL54;
        map[70] = Op.RETURN54;
        map[71] = Op.RETURN0;
        map[72] = Op.RETURN1;
        map[73] = Op.FORLOOP54;
        map[74] = Op.FORPREP54;
        map[75] = Op.TFORPREP54;
        map[76] = Op.TFORCALL54;
        map[77] = Op.TFORLOOP54;
        map[78] = Op.SETLIST54;
        map[79] = Op.CLOSURE;
        map[80] = Op.VARARG54;
        map[81] = Op.VARARGPREP;
        map[82] = Op.EXTRAARG;
        break;
      default:
        throw new IllegalStateException();
    }
    setup_lookup(true);
  }
  
  public Op get(int opNumber) {
    if(opNumber >= 0 && opNumber < map.length) {
      return map[opNumber];
    } else {
      return null;
    }
  }
  
  public Op get(String name) {
    return lookup.get(name);
  }
  
  public int size() {
    return map.length;
  }
  
  private void init_lookup() {
    lookup = new HashMap<String, Op>();
  }
  
  private void allow_51_math_lookup() {
    Op[] ops = {Op.MOD, Op.LEN};
    allow_ops_lookup(ops);
  }
  
  private void allow_53_math_lookup() {
    Op[] ops = {Op.IDIV, Op.BAND, Op.BOR, Op.BXOR, Op.SHL, Op.SHR, Op.BNOT};
    allow_ops_lookup(ops);
  }
  
  private void allow_ops_lookup(Op[] ops) {
    for(Op op : ops) {
      String name = op.name;
      if(!lookup.containsKey(name)) {
        lookup.put(name, op);
      } else {
        throw new IllegalStateException();
      }
    }
  }
  
  private void setup_lookup(boolean validate) {
    for(int i = 0; i < map.length; i++) {
      if(map[i] != null) {
        String name = map[i].name;
        if(!lookup.containsKey(name)) {
          lookup.put(name, map[i]);
        } else if(validate) {
          throw new IllegalStateException(name);
        }
      } else if(validate) {
        throw new IllegalStateException();
      }
    }
  }
  
}

