package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import unluac.Version;
import unluac.assemble.Directive;
import unluac.decompile.CodeExtract;

abstract public class LHeaderType extends BObjectType<LHeader> {

  public static final LHeaderType TYPE50 = new LHeaderType50();
  public static final LHeaderType TYPE51 = new LHeaderType51();
  public static final LHeaderType TYPE52 = new LHeaderType52();
  public static final LHeaderType TYPE53 = new LHeaderType53();
  public static final LHeaderType TYPE54 = new LHeaderType54();
  
  public static LHeaderType get(Version.HeaderType type) {
    switch(type) {
      case LUA50: return TYPE50;
      case LUA51: return TYPE51;
      case LUA52: return TYPE52;
      case LUA53: return TYPE53;
      case LUA54: return TYPE54;
      default: throw new IllegalStateException();
    }
  }
  
  private static final byte[] luacTail = {
    0x19, (byte) 0x93, 0x0D, 0x0A, 0x1A, 0x0A,
  };
  
  protected static final int TEST_INTEGER = 0x5678;
  
  protected static final double TEST_FLOAT = 370.5;
  
  protected static class LHeaderParseState {
    BIntegerType integer;
    BIntegerType sizeT;
    LNumberType number;
    LNumberType linteger;
    LNumberType lfloat;
    
    int format;
    LHeader.LEndianness endianness;
    
    int lNumberSize;
    boolean lNumberIntegrality;
    
    int lIntegerSize;
    int lFloatSize;
    
    int sizeOp;
    int sizeA;
    int sizeB;
    int sizeC;
  }
  
  @Override
  public LHeader parse(ByteBuffer buffer, BHeader header) {
    Version version = header.version;
    LHeaderParseState s = new LHeaderParseState();
    parse_main(buffer, header, s);
    LBooleanType bool = new LBooleanType();
    LStringType string = version.getLStringType();
    LConstantType constant = version.getLConstantType();
    LAbsLineInfoType abslineinfo = new LAbsLineInfoType();
    LLocalType local = new LLocalType();
    LUpvalueType upvalue = version.getLUpvalueType();
    LFunctionType function = version.getLFunctionType();
    CodeExtract extract = new CodeExtract(header.version, s.sizeOp, s.sizeA, s.sizeB, s.sizeC);
    return new LHeader(s.format, s.endianness, s.integer, s.sizeT, bool, s.number, s.linteger, s.lfloat, string, constant, abslineinfo, local, upvalue, function, extract);
  }
  
  abstract public List<Directive> get_directives();
  
  abstract protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s);
  
  protected void parse_format(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    // 1 byte Lua "format"
    int format = 0xFF & buffer.get();
    if(format != 0) {
      throw new IllegalStateException("The input chunk reports a non-standard lua format: " + format);
    }
    s.format = format;
    if(header.debug) {
      System.out.println("-- format: " + format);
    }
  }
  
  protected void write_format(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write(object.format);
  }
  
  protected void parse_endianness(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    // 1 byte endianness
    int endianness = 0xFF & buffer.get();
    switch(endianness) {
      case 0:
        s.endianness = LHeader.LEndianness.BIG;
        buffer.order(ByteOrder.BIG_ENDIAN);
        break;
      case 1:
        s.endianness = LHeader.LEndianness.LITTLE;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        break;
      default:
        throw new IllegalStateException("The input chunk reports an invalid endianness: " + endianness);
    }
    if(header.debug) {
      System.out.println("-- endianness: " + endianness + (endianness == 0 ? " (big)" : " (little)"));
    }
  }
  
  protected void write_endianness(OutputStream out, BHeader header, LHeader object) throws IOException {
    int value;
    switch(object.endianness) {
    case BIG:
      value = 0;
      break;
    case LITTLE:
      value = 1;
      break;
    default:
      throw new IllegalStateException();
    }
    out.write(value);
  }
  
  protected void parse_int_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    // 1 byte int size
    int intSize = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- int size: " + intSize);
    }
    s.integer = new BIntegerType50(intSize);
  }
  
  protected void write_int_size(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write(object.integer.getSize());
  }
  
  protected void parse_size_t_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    // 1 byte sizeT size
    int sizeTSize = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- size_t size: " + sizeTSize);
    }
    s.sizeT = new BIntegerType50(sizeTSize);
  }
  
  protected void write_size_t_size(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write(object.sizeT.getSize());
  }
  
  protected void parse_instruction_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    // 1 byte instruction size
    int instructionSize = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- instruction size: " + instructionSize);
    }
    if(instructionSize != 4) {
      throw new IllegalStateException("The input chunk reports an unsupported instruction size: " + instructionSize + " bytes");
    }
  }
  
  protected void write_instruction_size(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write(4);
  }
  
  protected void parse_number_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    int lNumberSize = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- Lua number size: " + lNumberSize);
    }
    s.lNumberSize = lNumberSize;
  }
  
  protected void write_number_size(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write(object.number.size);
  }
  
  protected void parse_number_integrality(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    int lNumberIntegralityCode = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- Lua number integrality code: " + lNumberIntegralityCode);
    }
    if(lNumberIntegralityCode > 1) {
      throw new IllegalStateException("The input chunk reports an invalid code for lua number integrality: " + lNumberIntegralityCode);
    }
    s.lNumberIntegrality = (lNumberIntegralityCode == 1);
  }
  
  protected void write_number_integrality(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write((byte)(object.number.integral ? 1 : 0));
  }
  
  protected void parse_integer_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    int lIntegerSize = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- Lua integer size: " + lIntegerSize);
    }
    if(lIntegerSize < 2) {
      throw new IllegalStateException("The input chunk reports an integer size that is too small: " + lIntegerSize);
    }
    s.lIntegerSize = lIntegerSize;
  }
  
  protected void parse_float_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    int lFloatSize = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- Lua float size: " + lFloatSize);
    }
    s.lFloatSize = lFloatSize;
  }
  
  protected void parse_number_format_53(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    byte[] endianness = new byte[s.lIntegerSize];
    buffer.get(endianness);
    
    byte test_high = (byte) ((TEST_INTEGER >> 8) & 0xFF);
    byte test_low = (byte) (TEST_INTEGER & 0xFF);
    
    if(endianness[0] == test_low && endianness[1] == test_high) {
      s.endianness = LHeader.LEndianness.LITTLE;
      buffer.order(ByteOrder.LITTLE_ENDIAN);
    } else if(endianness[s.lIntegerSize - 1] == test_low && endianness[s.lIntegerSize - 2] == test_high) {
      s.endianness = LHeader.LEndianness.BIG;
      buffer.order(ByteOrder.BIG_ENDIAN);
    } else {
      throw new IllegalStateException("The input chunk reports an invalid endianness: " + Arrays.toString(endianness));
    }
    s.linteger = new LNumberType(s.lIntegerSize, true, LNumberType.NumberMode.MODE_INTEGER);
    s.lfloat = new LNumberType(s.lFloatSize, false, LNumberType.NumberMode.MODE_FLOAT);
    double floatcheck = s.lfloat.parse(buffer, header).value();
    if(floatcheck != s.lfloat.convert(TEST_FLOAT)) {
      throw new IllegalStateException("The input chunk is using an unrecognized floating point format: " + floatcheck);
    }
  }
  
  protected void parse_extractor(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    s.sizeOp = 0xFF & buffer.get();
    s.sizeA = 0xFF & buffer.get();
    s.sizeB = 0xFF & buffer.get();
    s.sizeC = 0xFF & buffer.get();
    if(header.debug) {
      System.out.println("-- Lua opcode extractor sizeOp: " + s.sizeOp + ", sizeA: " + s.sizeA + ", sizeB: " + s.sizeB + ", sizeC: " + s.sizeC);
    }
  }
  
  protected void write_extractor(OutputStream out, BHeader header, LHeader object) throws IOException {
    out.write(object.extractor.op.size);
    out.write(object.extractor.A.size);
    out.write(object.extractor.B.size);
    out.write(object.extractor.C.size);
  }
  
  protected void parse_tail(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    for(int i = 0; i < luacTail.length; i++) {
      if(buffer.get() != luacTail[i]) {
        throw new IllegalStateException("The input file does not have the header tail of a valid Lua file (it may be corrupted).");
      }
    }
  }
  
  protected void write_tail(OutputStream out, BHeader header, LHeader object) throws IOException {
    for(int i = 0; i < luacTail.length; i++) {
      out.write(luacTail[i]);
    }
  }
  
}

class LHeaderType50 extends LHeaderType {
  
  private static final double TEST_NUMBER = 3.14159265358979323846E7;
  
  @Override
  protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    s.format = 0;
    parse_endianness(buffer, header, s);
    parse_int_size(buffer, header, s);
    parse_size_t_size(buffer, header, s);
    parse_instruction_size(buffer, header, s);
    parse_extractor(buffer, header, s);
    parse_number_size(buffer, header, s);
    LNumberType lfloat = new LNumberType(s.lNumberSize, false, LNumberType.NumberMode.MODE_NUMBER);
    LNumberType linteger = new LNumberType(s.lNumberSize, true, LNumberType.NumberMode.MODE_NUMBER);
    buffer.mark();
    double floatcheck = lfloat.parse(buffer, header).value();
    buffer.reset();
    double intcheck = linteger.parse(buffer, header).value();
    if(floatcheck == lfloat.convert(TEST_NUMBER)) {
      s.number = lfloat;
    } else if(intcheck == linteger.convert(TEST_NUMBER)) {
      s.number = linteger;
    } else {
      throw new IllegalStateException("The input chunk is using an unrecognized number format: " + intcheck);
    }
  }
  
  @Override
  public List<Directive> get_directives() {
    return Arrays.asList(new Directive[] {
       Directive.ENDIANNESS,
       Directive.INT_SIZE,
       Directive.SIZE_T_SIZE,
       Directive.INSTRUCTION_SIZE,
       Directive.SIZE_OP,
       Directive.SIZE_A,
       Directive.SIZE_B,
       Directive.SIZE_C,
       Directive.NUMBER_FORMAT,
    });
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
    write_endianness(out, header, object);
    write_int_size(out, header, object);
    write_size_t_size(out, header, object);
    write_instruction_size(out, header, object);
    write_extractor(out, header, object);
    write_number_size(out, header, object);
    object.number.write(out, header, object.number.create(TEST_NUMBER));
  }
  
}

class LHeaderType51 extends LHeaderType {
  
  @Override
  protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    parse_format(buffer, header, s);
    parse_endianness(buffer, header, s);
    parse_int_size(buffer, header, s);
    parse_size_t_size(buffer, header, s);
    parse_instruction_size(buffer, header, s);
    parse_number_size(buffer, header, s);
    parse_number_integrality(buffer, header, s);
    s.number = new LNumberType(s.lNumberSize, s.lNumberIntegrality, LNumberType.NumberMode.MODE_NUMBER);
  }
  
  @Override
  public List<Directive> get_directives() {
    return Arrays.asList(new Directive[] {
       Directive.FORMAT,
       Directive.ENDIANNESS,
       Directive.INT_SIZE,
       Directive.SIZE_T_SIZE,
       Directive.INSTRUCTION_SIZE,
       Directive.NUMBER_FORMAT,
    });
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
    write_format(out, header, object);
    write_endianness(out, header, object);
    write_int_size(out, header, object);
    write_size_t_size(out, header, object);
    write_instruction_size(out, header, object);
    write_number_size(out, header, object);
    write_number_integrality(out, header, object);
  }
  
}

class LHeaderType52 extends LHeaderType {
  
  @Override
  protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    parse_format(buffer, header, s);
    parse_endianness(buffer, header, s);
    parse_int_size(buffer, header, s);
    parse_size_t_size(buffer, header, s);
    parse_instruction_size(buffer, header, s);
    parse_number_size(buffer, header, s);
    parse_number_integrality(buffer, header, s);
    parse_tail(buffer, header, s);
    s.number = new LNumberType(s.lNumberSize, s.lNumberIntegrality, LNumberType.NumberMode.MODE_NUMBER);
  }
  
  @Override
  public List<Directive> get_directives() {
    return Arrays.asList(new Directive[] {
       Directive.FORMAT,
       Directive.ENDIANNESS,
       Directive.INT_SIZE,
       Directive.SIZE_T_SIZE,
       Directive.INSTRUCTION_SIZE,
       Directive.NUMBER_FORMAT,
    });
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
    write_format(out, header, object);
    write_endianness(out, header, object);
    write_int_size(out, header, object);
    write_size_t_size(out, header, object);
    write_instruction_size(out, header, object);
    write_number_size(out, header, object);
    write_number_integrality(out, header, object);
    write_tail(out, header, object);
  }
  
}

class LHeaderType53 extends LHeaderType {
  
  @Override
  protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    parse_format(buffer, header, s);
    parse_tail(buffer, header, s);
    parse_int_size(buffer, header, s);
    parse_size_t_size(buffer, header, s);
    parse_instruction_size(buffer, header, s);
    parse_integer_size(buffer, header, s);
    parse_float_size(buffer, header, s);
    parse_number_format_53(buffer, header, s);
  }
  
  @Override
  public List<Directive> get_directives() {
    return Arrays.asList(new Directive[] {
       Directive.FORMAT,
       Directive.INT_SIZE,
       Directive.SIZE_T_SIZE,
       Directive.INSTRUCTION_SIZE,
       Directive.INTEGER_FORMAT,
       Directive.FLOAT_FORMAT,
       Directive.ENDIANNESS,
    });
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
    write_format(out, header, object);
    write_tail(out, header, object);
    write_int_size(out, header, object);
    write_size_t_size(out, header, object);
    write_instruction_size(out, header, object);
    out.write(header.linteger.size);
    out.write(header.lfloat.size);
    header.linteger.write(out, header, header.linteger.create(TEST_INTEGER));
    header.lfloat.write(out, header, header.lfloat.create(TEST_FLOAT));
  }
  
}

class LHeaderType54 extends LHeaderType {
  
  @Override
  protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
    parse_format(buffer, header, s);
    parse_tail(buffer, header, s);
    parse_instruction_size(buffer, header, s);
    parse_integer_size(buffer, header, s);
    parse_float_size(buffer, header, s);
    parse_number_format_53(buffer, header, s);
    s.integer = new BIntegerType54();
    s.sizeT = new BIntegerType54();
  }
  
  @Override
  public List<Directive> get_directives() {
    return Arrays.asList(new Directive[] {
        Directive.FORMAT,
        Directive.INSTRUCTION_SIZE,
        Directive.INTEGER_FORMAT,
        Directive.FLOAT_FORMAT,
        Directive.ENDIANNESS,
     });
  }
  
  @Override
  public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
    write_format(out, header, object);
    write_tail(out, header, object);
    write_instruction_size(out, header, object);
    out.write(header.linteger.size);
    out.write(header.lfloat.size);
    header.linteger.write(out, header, header.linteger.create(TEST_INTEGER));
    header.lfloat.write(out, header, header.lfloat.create(TEST_FLOAT));
  }
  
}
