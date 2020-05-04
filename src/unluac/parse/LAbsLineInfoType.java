package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class LAbsLineInfoType extends BObjectType<LAbsLineInfo> {

  @Override
  public LAbsLineInfo parse(ByteBuffer buffer, BHeader header) {
    // TODO:
    header.integer.parse(buffer, header);
    header.integer.parse(buffer, header);
    return new LAbsLineInfo();
  }

  @Override
  public void write(OutputStream out, BHeader header, LAbsLineInfo object) throws IOException {
    // TODO Auto-generated method stub
    throw new IllegalStateException();
  }

}
