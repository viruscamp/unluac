package unluac.parse;

import unluac.decompile.CodeExtract;

public class LHeader extends BObject {

  public static enum LEndianness {
    BIG,
    LITTLE;
  }
  
  public final int format;
  public final LEndianness endianness;
  public final BIntegerType integer;
  public final BIntegerType sizeT;
  public final LBooleanType bool;
  public final LNumberType number;
  public final LNumberType linteger;
  public final LNumberType lfloat;
  public final LStringType string;
  public final LConstantType constant;
  public final LAbsLineInfoType abslineinfo;
  public final LLocalType local;
  public final LUpvalueType upvalue;
  public final LFunctionType function;
  public final CodeExtract extractor;
  
  public LHeader(int format, LEndianness endianness, BIntegerType integer, BIntegerType sizeT, LBooleanType bool, LNumberType number, LNumberType linteger, LNumberType lfloat, LStringType string, LConstantType constant, LAbsLineInfoType abslineinfo, LLocalType local, LUpvalueType upvalue, LFunctionType function, CodeExtract extractor) {
    this.format = format;
    this.endianness = endianness;
    this.integer = integer;
    this.sizeT = sizeT;
    this.bool = bool;
    this.number = number;
    this.linteger = linteger;
    this.lfloat = lfloat;
    this.string = string;
    this.constant = constant;
    this.abslineinfo = abslineinfo;
    this.local = local;
    this.upvalue = upvalue;
    this.function = function;
    this.extractor = extractor;
  }
  
}
