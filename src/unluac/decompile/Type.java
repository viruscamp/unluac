package unluac.decompile;

import java.util.HashMap;
import java.util.Map;

public enum Type {
  NIL("nil"),
  BOOLEAN("boolean"),
  FALSE("false"),
  TRUE("true"),
  NUMBER("number"),
  FLOAT("float"),
  INTEGER("integer"),
  STRING("string"),
  SHORT_STRING("short_string"),
  LONG_STRING("long_string");
  
  private static Map<String, Type> lookup = null;
  
  public final String name;
  
  private Type(String name) {
    this.name = name;
  }
  
  private static void initialize_lookup() {
    if(lookup == null) {
      lookup = new HashMap<String, Type>();
      for(Type type : values()) {
        lookup.put(type.name, type);
      }
    }
  }
  
  public static Type get(String name) {
    initialize_lookup();
    return lookup.get(name);
  }
  
}
