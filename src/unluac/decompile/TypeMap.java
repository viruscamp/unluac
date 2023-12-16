package unluac.decompile;

import java.util.Map;

import unluac.Version;

public class TypeMap {
  
  private Type[] types;
  
  public final int NIL;
  public final int BOOLEAN;
  public final int FALSE;
  public final int TRUE;
  public final int NUMBER;
  public final int FLOAT;
  public final int INTEGER;
  public final int STRING;
  public final int SHORT_STRING;
  public final int LONG_STRING;
  
  public static final int UNMAPPED = -1;
  
  private static final int VARIANT = (1 << 4);
  
  public TypeMap(Version.TypeMapType version) {
    boolean split_string = false;
    boolean split_number = false;
    boolean split_boolean = false;
    boolean reverse_number = false;
    switch(version) {
      case LUA50:
        break;
      case LUA52:
        split_string = true;
        break;
      case LUA53:
        split_string = true;
        split_number = true;
        break;
      case LUA54:
        split_string = true;
        split_number = true;
        split_boolean = true;
        reverse_number = true;
        break;
    }
    types = new Type[split_string ? (5 + VARIANT) : 5];
    
    types[0] = Type.NIL;
    NIL = 0;
    
    if(!split_boolean) {
      BOOLEAN = 1;
      FALSE = UNMAPPED;
      TRUE = UNMAPPED;
      types[BOOLEAN] = Type.BOOLEAN;
    } else {
      BOOLEAN = UNMAPPED;
      FALSE = 1;
      TRUE = 1 | VARIANT;
      types[FALSE] = Type.FALSE;
      types[TRUE] = Type.TRUE;
    }
    
    if(!split_number) {
      NUMBER = 3;
      FLOAT = UNMAPPED;
      INTEGER = UNMAPPED;
      types[NUMBER] = Type.NUMBER;
    } else {
      NUMBER = UNMAPPED;
      if(!reverse_number) {
        FLOAT = 3;
        INTEGER = 3 | VARIANT;
      } else {
        INTEGER = 3;
        FLOAT = 3 | VARIANT;
      }
      types[FLOAT] = Type.FLOAT;
      types[INTEGER] = Type.INTEGER;
    }
    
    if(!split_string) {
      STRING = 4;
      SHORT_STRING = UNMAPPED;
      LONG_STRING = UNMAPPED;
      types[STRING] = Type.STRING;
    } else {
      STRING = UNMAPPED;
      SHORT_STRING = 4;
      LONG_STRING = 4 | VARIANT;
      types[SHORT_STRING] = Type.SHORT_STRING;
      types[LONG_STRING] = Type.LONG_STRING;
    }
  }
  
  public TypeMap(Map<Integer, Type> usertypemap) {
    int maximum = 0;
    for(Integer typecode : usertypemap.keySet()) {
      maximum = Math.max(maximum, typecode);
    }
    types = new Type[maximum + 1];
    int user_nil = UNMAPPED;
    int user_boolean = UNMAPPED;
    int user_false = UNMAPPED;
    int user_true = UNMAPPED;
    int user_number = UNMAPPED;
    int user_float = UNMAPPED;
    int user_integer = UNMAPPED;
    int user_string = UNMAPPED;
    int user_short_string = UNMAPPED;
    int user_long_string = UNMAPPED;
    for(Map.Entry<Integer, Type> entry : usertypemap.entrySet()) {
      int typecode = entry.getKey();
      Type type = entry.getValue();
      types[typecode] = type;
      switch(type) {
        case NIL: user_nil = typecode; break;
        case BOOLEAN: user_boolean = typecode; break;
        case FALSE: user_false = typecode; break;
        case TRUE: user_true = typecode; break;
        case NUMBER: user_number = typecode; break;
        case FLOAT: user_float = typecode; break;
        case INTEGER: user_integer = typecode; break;
        case STRING: user_string = typecode; break;
        case SHORT_STRING: user_short_string = typecode; break;
        case LONG_STRING: user_long_string = typecode; break;
      }
    }
    NIL = user_nil;
    BOOLEAN = user_boolean;
    FALSE = user_false;
    TRUE = user_true;
    NUMBER = user_number;
    FLOAT = user_float;
    INTEGER = user_integer;
    STRING = user_string;
    SHORT_STRING = user_short_string;
    LONG_STRING = user_long_string;
  }
  
  public int size() {
    return types.length;
  }
  
  public Type get(int typecode) {
    if(typecode >= 0 && typecode < types.length) {
      return types[typecode];
    } else {
      return null;
    }
  }
  
}
