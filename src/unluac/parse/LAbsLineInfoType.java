package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class LAbsLineInfoType extends BObjectType<LAbsLineInfo> {

  @Override
  public LAbsLineInfo parse(ByteBuffer buffer, BHeader header) {
    int pc = header.integer.parse(buffer, header).asInt();
    int line = header.integer.parse(buffer, header).asInt();
    return new LAbsLineInfo(pc, line);
  }

  @Override
  public void write(OutputStream out, BHeader header, LAbsLineInfo object) throws IOException {
    // TODO Auto-generated method stub
    throw new IllegalStateException();
  }

}
