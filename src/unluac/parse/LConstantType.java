package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import unluac.decompile.Type;
import unluac.decompile.TypeMap;


public class LConstantType extends BObjectType<LObject> {
  
  @Override
  public LObject parse(ByteBuffer buffer, BHeader header) {
    int typecode = 0xFF & buffer.get();
    Type type = header.typemap.get(typecode);
    if(header.debug) {
      System.out.print("-- parsing <constant>, type " + typecode + " is ");
      if(type == null) System.out.println("unknown");
      else System.out.println(type);
    }
    if(type == null) {
      throw new RuntimeException("unmapped type code " + typecode);
    }
    switch(type) {
      case NIL:
        return LNil.NIL;
      case BOOLEAN:
        return header.bool.parse(buffer, header);
      case FALSE:
        return LBoolean.LFALSE;
      case TRUE:
        return LBoolean.LTRUE;
      case NUMBER:
        return header.number.parse(buffer, header);
      case FLOAT:
        return header.lfloat.parse(buffer, header);
      case INTEGER:
        return header.linteger.parse(buffer, header);
      case STRING:
      case SHORT_STRING:
        return header.string.parse(buffer, header);
      case LONG_STRING: {
        LString s = header.string.parse(buffer, header);
        s.islong = true;
        return s;
      }
      default:
        throw new IllegalStateException();
    }
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LObject object) throws IOException {
    if(object instanceof LNil) {
      if(header.typemap.NIL == TypeMap.UNMAPPED) throw new IllegalStateException();
      out.write(header.typemap.NIL);
    } else if(object instanceof LBoolean) {
      LBoolean b = (LBoolean) object;
      boolean value = b.value();
      if(value && header.typemap.TRUE != TypeMap.UNMAPPED) {
        out.write(header.typemap.TRUE);
      } else if(!value && header.typemap.FALSE != TypeMap.UNMAPPED) {
        out.write(header.typemap.FALSE);
      } else if(header.typemap.BOOLEAN != TypeMap.UNMAPPED) {
        out.write(header.typemap.BOOLEAN);
        header.bool.write(out, header, b);
      } else {
        throw new IllegalStateException();
      }
    } else if(object instanceof LNumber) {
      LNumber n = (LNumber) object;
      if(header.typemap.FLOAT != TypeMap.UNMAPPED && !n.integralType()) {
        out.write(header.typemap.FLOAT);
        header.lfloat.write(out, header, n);
      } else if(header.typemap.INTEGER != TypeMap.UNMAPPED && n.integralType()) {
        out.write(header.typemap.INTEGER);
        header.linteger.write(out, header, n);
      } else if(header.typemap.NUMBER != TypeMap.UNMAPPED) {
        out.write(header.typemap.NUMBER);
        header.number.write(out, header, n);
      } else {
        throw new IllegalStateException();
      }
    } else if(object instanceof LString) {
      LString s = (LString) object;
      if(header.typemap.SHORT_STRING != TypeMap.UNMAPPED && !s.islong) {
        out.write(header.typemap.SHORT_STRING);
      } else if(header.typemap.LONG_STRING != TypeMap.UNMAPPED && s.islong) {
        out.write(header.typemap.LONG_STRING);
      } else if(header.typemap.STRING != TypeMap.UNMAPPED) {
        out.write(header.typemap.STRING);
      }
      header.string.write(out, header, s);
    } else {
      throw new IllegalStateException();
    }
  }
  
}
