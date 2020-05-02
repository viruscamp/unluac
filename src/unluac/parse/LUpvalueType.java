package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class LUpvalueType extends BObjectType<LUpvalue> {

  @Override
  public LUpvalue parse(ByteBuffer buffer, BHeader header) {
    LUpvalue upvalue = new LUpvalue();
    upvalue.instack = buffer.get() != 0;
    upvalue.idx = 0xFF & buffer.get();
    return upvalue;
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LUpvalue object) throws IOException {
    out.write((byte)(object.instack ? 1 : 0));
    out.write(object.idx);
  }
}