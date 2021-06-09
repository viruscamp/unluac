package unluac.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import unluac.Configuration;
import unluac.Version;
import unluac.assemble.Tokenizer;
import unluac.decompile.CodeExtract;
import unluac.decompile.Op;
import unluac.decompile.OpcodeMap;


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
  public final OpcodeMap opmap;
  
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
    opmap = version.getOpcodeMap();
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
    
    if(config.opmap != null) {
      try {
        Tokenizer t = new Tokenizer(new BufferedReader(new FileReader(new File(config.opmap))));
        String tok;
        Map<Integer, Op> useropmap = new HashMap<Integer, Op>();
        while((tok = t.next()) != null) {
          if(tok.equals(".op")) {
            tok = t.next();
            if(tok == null) throw new IllegalStateException("Unexpected end of opmap file.");
            int opcode;
            try {
              opcode = Integer.parseInt(tok);
            } catch(NumberFormatException e) {
              throw new IllegalStateException("Excepted number in opmap file, got \"" + tok + "\".");
            }
            tok = t.next();
            if(tok == null) throw new IllegalStateException("Unexpected end of opmap file.");
            Op op = version.getOpcodeMap().get(tok);
            if(op == null) throw new IllegalStateException("Unknown op name \"" + tok + "\" in opmap file.");
            useropmap.put(opcode, op);
          } else {
            throw new IllegalStateException("Unexpected token \"" + tok + "\" + in opmap file.");
          }
        }
        opmap = new OpcodeMap(useropmap);
      } catch(IOException e) {
        throw new IllegalStateException(e.getMessage());
      }
    } else {
      opmap = version.getOpcodeMap();
    }
    
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
