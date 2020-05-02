package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;


public class LNumberType extends BObjectType<LNumber> {

  public static enum NumberMode {
    MODE_NUMBER, // Used for Lua 5.0 - 5.2 where numbers can represent integers or floats
    MODE_FLOAT, // Used for floats in Lua 5.3
    MODE_INTEGER, // Used for integers in Lua 5.3
  }
  
  public final int size;
  public final boolean integral;
  public final NumberMode mode;
  
  public LNumberType(int size, boolean integral, NumberMode mode) {
    this.size = size;
    this.integral = integral;
    this.mode = mode;
    if(!(size == 4 || size == 8)) {
      throw new IllegalStateException("The input chunk has an unsupported Lua number size: " + size);
    }
  }
  
  public double convert(double number) {
    if(integral) {
      switch(size) {
        case 4:
          return (int)number;
        case 8:
          return (long)number;
      }
    } else {
      switch(size) {
        case 4:
          return (float)number;
        case 8:
          return number;
      }
    }
    throw new IllegalStateException("The input chunk has an unsupported Lua number format");
  }
  
  @Override
  public LNumber parse(ByteBuffer buffer, BHeader header) {
    LNumber value = null;
    if(integral) {
      switch(size) {
        case 4:
          value = new LIntNumber(buffer.getInt());
          break;
        case 8:
          value = new LLongNumber(buffer.getLong());
          break;
      }
    } else {
      switch(size) {
        case 4:
          value = new LFloatNumber(buffer.getFloat(), mode);
          break;
        case 8:
          value = new LDoubleNumber(buffer.getDouble(), mode);
          break;
      }
    }
    if(value == null) {
      throw new IllegalStateException("The input chunk has an unsupported Lua number format");
    }
    if(header.debug) {
      System.out.println("-- parsed <number> " + value);
    }
    return value;
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LNumber n) throws IOException {
    long bits = n.bits();
    if(header.lheader.endianness == LHeader.LEndianness.LITTLE) {
      for(int i = 0; i < size; i++) {
        out.write((byte)(bits & 0xFF));
        bits = bits >>> 8;
      }
    } else {
      for(int i = size - 1; i >= 0; i--) {
        out.write((byte)((bits >> (i * 8)) & 0xFF));
      }
    }
  }
  
  public LNumber create(double x) {
    if(integral) {
      switch(size) {
        case 4:
          return new LIntNumber((int) x);
        case 8:
          return new LLongNumber((long) x);
        default:
          throw new IllegalStateException();
      }
    } else {
      switch(size) {
        case 4:
          return new LFloatNumber((float) x, mode);
        case 8:
          return new LDoubleNumber(x, mode);
        default:
          throw new IllegalStateException();
      }
    }
  }
  
  public LNumber create(BigInteger x) {
    if(integral) {
      switch(size) {
        case 4:
          return new LIntNumber(x.intValueExact());
        case 8:
          return new LLongNumber(x.longValueExact());
        default:
          throw new IllegalStateException();
      }
    } else {
      switch(size) {
        case 4:
          return new LFloatNumber(x.floatValue(), mode);
        case 8:
          return new LDoubleNumber(x.doubleValue(), mode);
        default:
          throw new IllegalStateException();
      }
    }
  }

}
