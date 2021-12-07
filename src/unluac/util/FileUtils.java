package unluac.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class FileUtils {

  public static InputStream createSmartTextFileReader(File file) throws IOException {
    BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
    byte[] header = new byte[2];
    int header_length = 0;
    input.mark(header.length);
    do {
      int n = input.read(header, header_length, header.length - header_length);
      if(n == -1) break;
      header_length += n;
    } while(header_length < header.length);
    if(header.length >= 2 && header[0] == (byte)0xff && header[1] == (byte)0xfe) {
      return readerToUTF8Stream(new InputStreamReader(input, "UTF-16LE"));
    } else if(header.length >= 2 && header[0] == (byte)0xfe && header[1] == (byte)0xff) {
      return readerToUTF8Stream(new InputStreamReader(input, "UTF-16BE"));
    } else {
      input.reset();
      return input;
    }
  }
  
  public static InputStream readerToUTF8Stream(Reader r) {
    final CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
    final CharBuffer chars = CharBuffer.allocate(2);
    final ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.limit(0);
    return new InputStream() {

      @Override
      public int read() throws IOException {
        if(buffer.position() < buffer.limit()) {
          return 0xff & buffer.get();
        }
        int result = r.read();
        if(result <= 127) {
          return result;
        }
        char c = (char)result;
        chars.position(0);
        chars.limit(1);
        chars.put(c);
        if(Character.isSurrogate(c)) {
          result = r.read();
          if(result >= 0) {
            chars.limit(2);
            chars.put((char) result);
          }
        }
        chars.position(0);
        buffer.limit(buffer.capacity());
        buffer.position(0);
        encoder.reset();
        CoderResult coderResult = encoder.encode(chars, buffer, true);
        if(coderResult.isError() || coderResult.isOverflow()) {
          throw new IllegalStateException(coderResult.toString());
        }
        buffer.limit(buffer.position());
        buffer.position(0);
        return read();
      }
      
    };
  }
  
  private FileUtils() {}
  
}
