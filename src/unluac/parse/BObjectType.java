package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import unluac.Version;

abstract public class BObjectType<T extends BObject>  {
  
  abstract public T parse(ByteBuffer buffer, BHeader header);

  abstract public void write(OutputStream out, BHeader header, T object) throws IOException;
  
  public final BList<T> parseList(ByteBuffer buffer, BHeader header) {
    return parseList(buffer, header, Version.ListLengthMode.STRICT, null);
  }
  
  public final BList<T> parseList(ByteBuffer buffer, BHeader header, Version.ListLengthMode mode) {
    return parseList(buffer, header, mode, null);
  }
  
  public final BList<T> parseList(ByteBuffer buffer, BHeader header, Version.ListLengthMode mode, BInteger knownLength) {
    BInteger length = header.integer.parse(buffer, header);
    switch(mode) {
      case STRICT:
        break;
      case ALLOW_NEGATIVE:
        if(length.signum() < 0) length = new BInteger(0);
        break;
      case IGNORE:
        if(knownLength == null) throw new IllegalStateException();
        if(length.signum() != 0) length = knownLength;
        break;
    }
    return parseList(buffer, header, length);
  }
  
  public final BList<T> parseList(final ByteBuffer buffer, final BHeader header, BInteger length) {
    final List<T> values = new ArrayList<T>();
    length.iterate(new Runnable() {
      
      @Override
      public void run() {
        values.add(parse(buffer, header));
      }
      
    });
    return new BList<T>(length, values);
  }
  
  public final void writeList(OutputStream out, BHeader header, T[] array) throws IOException {
    header.integer.write(out, header, new BInteger(array.length));
    for(T object : array) {
      write(out, header, object);
    }
  }
  
  public final void writeList(OutputStream out, BHeader header, BList<T> blist) throws IOException {
    header.integer.write(out, header, blist.length);
    Iterator<T> it = blist.iterator();
    while(it.hasNext()) {
      write(out, header, it.next());
    }
  }
  
}
