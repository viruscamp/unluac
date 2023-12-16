package unluac.decompile;

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
  
  public Type get(int typecode) {
    if(typecode >= 0 && typecode < types.length) {
      return types[typecode];
    } else {
      return null;
    }
  }
  
}
