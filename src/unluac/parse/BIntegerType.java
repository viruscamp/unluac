package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BIntegerType extends BObjectType<BInteger> {

  public final int intSize;
  
  public BIntegerType(int intSize) {
    this.intSize = intSize;
  }
  
  protected BInteger raw_parse(ByteBuffer buffer, BHeader header) {
    BInteger value;
    switch(intSize) {
      case 0:
        value = new BInteger(0);
        break;
      case 1:
        value = new BInteger(buffer.get());
        break;
      case 2:
        value = new BInteger(buffer.getShort());
        break;
      case 4:
        value = new BInteger(buffer.getInt());
        break;
      default: {
        byte[] bytes = new byte[intSize];
        int start = 0;
        int delta = 1;
        if(buffer.order() == ByteOrder.LITTLE_ENDIAN) {
          start = intSize - 1;
          delta = -1;
        }
        for(int i = start; i >= 0 && i < intSize; i += delta) {
          bytes[i] = buffer.get();
        }
        value = new BInteger(new BigInteger(bytes));
      }
        
    }
    return value;
  }
  
  protected void raw_write(OutputStream out, BHeader header, BInteger object) throws IOException {
    byte[] bytes = object.littleEndianBytes(intSize);
    if(header.lheader.endianness == LHeader.LEndianness.LITTLE) {
      for(byte b : bytes) {
        out.write(b);
      }
    } else {
      for(int i = bytes.length - 1; i >= 0; i--) {
        out.write(bytes[i]);
      }
    }
  }
  
  @Override
  public BInteger parse(ByteBuffer buffer, BHeader header) {
    BInteger value = raw_parse(buffer, header);
    if(header.debug){
      System.out.println("-- parsed <integer> " + value.asInt());
    }
    return value;
  }
  
  @Override
  public void write(OutputStream out, BHeader header, BInteger object) throws IOException {
    raw_write(out, header, object);
  }
  
}
