package unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import unluac.Configuration;
import unluac.Version;
import unluac.decompile.CodeExtract;


public class BHeader {

  private static final byte[] signature = {
    0x1B, 0x4C, 0x75, 0x61,
  };
  
  public final boolean debug = false;
  
  public final Configuration config;
  public final Version version;
  public final LHeader lheader;
  public final LHeaderType lheader_type;
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
  
  public final LFunction main;
  
  public BHeader(Version version, LHeader lheader) {
    this(version, lheader, null);
  }
  
  public BHeader(Version version, LHeader lheader, LFunction main) {
    this.config = null;
    this.version = version;
    this.lheader = lheader;
    this.lheader_type = version.getLHeaderType();
    integer = lheader.integer;
    sizeT = lheader.sizeT;
    bool = lheader.bool;
    number = lheader.number;
    linteger = lheader.linteger;
    lfloat = lheader.lfloat;
    string = lheader.string;
    constant = lheader.constant;
    abslineinfo = lheader.abslineinfo;
    local = lheader.local;
    upvalue = lheader.upvalue;
    function = lheader.function;
    extractor = lheader.extractor;
    this.main = main;
  }
  
  public BHeader(ByteBuffer buffer, Configuration config) {
    this.config = config;
    // 4 byte Lua signature
    for(int i = 0; i < signature.length; i++) {
      if(buffer.get() != signature[i]) {
        throw new IllegalStateException("The input file does not have the signature of a valid Lua file.");
      }
    }
    
    int versionNumber = 0xFF & buffer.get();
    int major = versionNumber >> 4;
    int minor = versionNumber & 0x0F;
    
    version = Version.getVersion(major, minor);
    if(version == null) {
      throw new IllegalStateException("The input chunk's Lua version is " + major + "." + minor + "; unluac can only handle Lua 5.0 - Lua 5.4.");
    }
    
    lheader_type = version.getLHeaderType();
    lheader = lheader_type.parse(buffer, this);
    integer = lheader.integer;
    sizeT = lheader.sizeT;
    bool = lheader.bool;
    number = lheader.number;
    linteger = lheader.linteger;
    lfloat = lheader.lfloat;
    string = lheader.string;
    constant = lheader.constant;
    abslineinfo = lheader.abslineinfo;
    local = lheader.local;
    upvalue = lheader.upvalue;
    function = lheader.function;
    extractor = lheader.extractor;
    
    int upvalues = -1;
    if(versionNumber >= 0x53) {
      upvalues = 0xFF & buffer.get();
      if(debug) {
        System.out.println("-- main chunk upvalue count: " + upvalues);
      }
      // TODO: check this value
    }
    main = function.parse(buffer, this);
    if(upvalues >= 0) {
      if(main.numUpvalues != upvalues) {
        throw new IllegalStateException("The main chunk has the wrong number of upvalues: " + main.numUpvalues + " (" + upvalues + " expected)");
      }
    }
    if(main.numUpvalues >= 1 && versionNumber >= 0x52 && (main.upvalues[0].name == null || main.upvalues[0].name.isEmpty())) {
      main.upvalues[0].name = "_ENV";
    }
    main.setLevel(1);
  }
  
  public void write(OutputStream out) throws IOException {
    out.write(signature);
    int major = version.getVersionMajor();
    int minor = version.getVersionMinor();
    int versionNumber = (major << 4) | minor;
    out.write(versionNumber);
    version.getLHeaderType().write(out, this, lheader);
    if(version.useupvaluecountinheader.get()) {
      out.write(main.numUpvalues);
    }
    function.write(out, this, main);
  }
  
}
