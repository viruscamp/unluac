package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import unluac.Version;

public abstract class LUpvalueType extends BObjectType<LUpvalue> {
  
  public static LUpvalueType get(Version version) {
    if(version.getVersionNumber() >= 0x54) {
      return new LUpvalueType54();
    } else {
      return new LUpvalueType50();
    }
  }
  
}

class LUpvalueType50 extends LUpvalueType {

  @Override
  public LUpvalue parse(ByteBuffer buffer, BHeader header) {
    LUpvalue upvalue = new LUpvalue();
    upvalue.instack = buffer.get() != 0;
    upvalue.idx = 0xFF & buffer.get();
    upvalue.kind = -1;
    return upvalue;
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LUpvalue object) throws IOException {
    out.write((byte)(object.instack ? 1 : 0));
    out.write(object.idx);
  }
}

class LUpvalueType54 extends LUpvalueType {

  @Override
  public LUpvalue parse(ByteBuffer buffer, BHeader header) {
    LUpvalue upvalue = new LUpvalue();
    upvalue.instack = buffer.get() != 0;
    upvalue.idx = 0xFF & buffer.get();
    upvalue.kind = 0xFF & buffer.get();
    return upvalue;
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LUpvalue object) throws IOException {
    out.write((byte)(object.instack ? 1 : 0));
    out.write(object.idx);
    out.write(object.kind);
  }
}
