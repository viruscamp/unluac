package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import unluac.Version;


public abstract class LStringType extends BObjectType<LString> {

  public static LStringType get(Version.StringType type) {
    switch(type) {
      case LUA50: return new LStringType50();
      case LUA53: return new LStringType53();
      case LUA54: return new LStringType54();
      default: throw new IllegalStateException();
    }
  }
  
  protected ThreadLocal<StringBuilder> b = new ThreadLocal<StringBuilder>() {
    
    @Override
    protected StringBuilder initialValue() {
      return new StringBuilder();  
    }

  };
  
}

class LStringType50 extends LStringType {
  
  @Override
  public LString parse(final ByteBuffer buffer, BHeader header) {
    BInteger sizeT = header.sizeT.parse(buffer, header);
//    final StringBuilder b = this.b.get();
//    b.setLength(0);
//    sizeT.iterate(new Runnable() {
//
//      @Override
//      public void run() {
//        b.append((char) (0xFF & buffer.get()));
//      }
//
//    });
//    if(b.length() == 0) {
//      return LString.NULL;
//    } else {
//      char last = b.charAt(b.length() - 1);
//      b.delete(b.length() - 1, b.length());
//      String s = b.toString();
//      if(header.debug) {
//        System.out.println("-- parsed <string> \"" + s + "\"");
//      }
//      return new LString(s, last);
//    }
    int length = sizeT.asInt();
    if(length == 0) {
      return LString.NULL;
    } else {
      byte[] bytes = new byte[length - 1];
      buffer.get(bytes);
      String s = new String(bytes, StandardCharsets.UTF_8);

      char last = (char) (0xFF & buffer.get());
      if (header.debug) {
        System.out.println("-- parsed <string> \"" + s + "\"");
      }
      return new LString(s, last);
    }
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LString string) throws IOException {
    int len = string.value.length();
    if(string == LString.NULL) {
      header.sizeT.write(out, header, header.sizeT.create(0));
    } else {
      header.sizeT.write(out, header, header.sizeT.create(len + 1));
      byte[] bytes = string.value.getBytes(StandardCharsets.UTF_8);
      out.write(bytes);
      out.write(0);
    }
  }
}

class LStringType53 extends LStringType {
  
  @Override
  public LString parse(final ByteBuffer buffer, BHeader header) {
    BInteger sizeT;
    int length = 0xFF & buffer.get();
    if(length == 0) {
      return LString.NULL;
    } else if(length == 0xFF) {
      sizeT = header.sizeT.parse(buffer, header);
      length = sizeT.asInt();
    } else {
      sizeT = new BInteger(length);
    }
//    final StringBuilder b = this.b.get();
//    b.setLength(0);
//    sizeT.iterate(new Runnable() {
//
//      boolean first = true;
//
//      @Override
//      public void run() {
//        if(!first) {
//          b.append((char) (0xFF & buffer.get()));
//        } else {
//          first = false;
//        }
//      }
//
//    });
//    String s = b.toString();
//    if(header.debug) {
//      System.out.println("-- parsed <string> \"" + s + "\"");
//    }
//    return new LString(s);
    byte[] bytes = new byte[length - 1];
    buffer.get(bytes);
    String s = new String(bytes, StandardCharsets.UTF_8);

    if (header.debug) {
      System.out.println("-- parsed <string> \"" + s + "\"");
    }
    return new LString(s);
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LString string) throws IOException {
    if(string == LString.NULL) {
      out.write(0);
    } else {
      int len = string.value.length() + 1;
      if(len < 0xFF) {
        out.write((byte)len);
      } else {
        out.write(0xFF);
        header.sizeT.write(out, header, header.sizeT.create(len));
      }
      byte[] bytes = string.value.getBytes(StandardCharsets.UTF_8);
      out.write(bytes);
    }
  }
}

class LStringType54 extends LStringType {
  
  @Override
  public LString parse(final ByteBuffer buffer, BHeader header) {
    BInteger sizeT = header.sizeT.parse(buffer, header);
    int length = sizeT.asInt();
    if(length == 0) {
      return LString.NULL;
    }
//    final StringBuilder b = this.b.get();
//    b.setLength(0);
//    sizeT.iterate(new Runnable() {
//
//      boolean first = true;
//
//      @Override
//      public void run() {
//        if(!first) {
//          b.append((char) (0xFF & buffer.get()));
//        } else {
//          first = false;
//        }
//      }
//
//    });
//    String s = b.toString();
    byte[] bytes = new byte[length - 1];
    buffer.get(bytes);
    String s = new String(bytes, StandardCharsets.UTF_8);

    if (header.debug) {
      System.out.println("-- parsed <string> \"" + s + "\"");
    }
    return new LString(s);
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LString string) throws IOException {
    if(string == LString.NULL) {
      header.sizeT.write(out, header, header.sizeT.create(0));
    } else {
      header.sizeT.write(out, header, header.sizeT.create(string.value.length() + 1));
      byte[] bytes = string.value.getBytes(StandardCharsets.UTF_8);
      out.write(bytes);
    }
  }
}

