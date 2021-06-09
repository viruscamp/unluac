package unluac.decompile;

import unluac.assemble.Directive;
import unluac.parse.LAbsLineInfo;
import unluac.parse.LFunction;
import unluac.parse.LLocal;
import unluac.parse.LUpvalue;
import unluac.util.StringUtils;

public class Disassembler {

  private final LFunction function;
  private final Code code;
  private final String name;
  private final String parent;
  
  public Disassembler(LFunction function) {
    this(function, "main", null);
  }
  
  private Disassembler(LFunction function, String name, String parent) {
    this.function = function;
    this.code = new Code(function);
    this.name = name;
    this.parent = parent;
  }
  
  public void disassemble(Output out) {
    disassemble(out, 0, 0);
  }
  
  private void disassemble(Output out, int level, int index) {
    if(parent == null) {
      out.println(".version\t" + function.header.version.getName());
      out.println();
      
      for(Directive directive : function.header.lheader_type.get_directives()) {
        directive.disassemble(out, function.header, function.header.lheader);
      }
      out.println();
      
      if(function.header.opmap != function.header.version.getOpcodeMap()) {
        OpcodeMap opmap = function.header.opmap;
        for(int opcode = 0; opcode < opmap.size(); opcode++) {
          Op op = opmap.get(opcode);
          if(op != null) {
            out.println(Directive.OP.token + "\t" + opcode + "\t" + op.name);
          }
        }
        out.println();
      }
    }
    
    String fullname;
    if(parent == null) {
      fullname = name;
    } else {
      fullname = parent + "/" + name;
    }
    out.println(".function\t" + fullname);
    out.println();
    
    for(Directive directive : function.header.function.get_directives()) {
      directive.disassemble(out, function.header, function);
    }
    out.println();
    
    if(function.locals.length > 0) {
      for(int local = 1; local <= function.locals.length; local++) {
        LLocal l = function.locals[local - 1];
        out.println(".local\t" + l.name.toPrintString() + "\t" + l.start + "\t" + l.end);
      }
      out.println();
    }
    
    if(function.upvalues.length > 0) {
      for(int upvalue = 1; upvalue <= function.upvalues.length; upvalue++) {
        LUpvalue u = function.upvalues[upvalue - 1];
        out.println(".upvalue\t" + StringUtils.toPrintString(u.name) + "\t" + u.idx + "\t" + u.instack);
      }
      out.println();
    }
    
    if(function.constants.length > 0) {
      for(int constant = 1; constant <= function.constants.length; constant++) {
        out.println(".constant\tk" + (constant - 1) + "\t" + function.constants[constant - 1].toPrintString());
      }
      out.println();
    }
    
    boolean[] label = new boolean[function.code.length];
    for(int line = 1; line <= function.code.length; line++) {
      Op op = code.op(line);
      if(op != null && op.hasJump()) {
        int target = code.target(line);
        if(target >= 1 && target <= label.length) {
          label[target - 1] = true;
        }
      }
    }
    
    int abslineinfoindex = 0;
    
    for(int line = 1; line <= function.code.length; line++) {
      if(label[line - 1]) {
        out.println(".label\t" + "l" + line);
      }
      if(function.abslineinfo != null && abslineinfoindex < function.abslineinfo.length && function.abslineinfo[abslineinfoindex].pc == line - 1) {
        LAbsLineInfo info = function.abslineinfo[abslineinfoindex++];
        out.println(".abslineinfo\t" + info.pc + "\t" + info.line);
      }
      if(line <= function.lines.length) {
        out.print(".line\t" + function.lines[line - 1] + "\t");
      }
      Op op = code.op(line);
      String cpLabel = null;
      if(op != null && op.hasJump()) {
        int target = code.target(line);
        if(target >= 1 && target <= code.length) {
          cpLabel = "l" + target;
        }
      }
      if(op == null) {
        out.println(Op.defaultToString(code.codepoint(line), function.header.version, code.getExtractor()));
      } else {
        out.println(op.codePointToString(code.codepoint(line), code.getExtractor(), cpLabel));
      }
      //out.println("\t" + code.opcode(line) + " " + code.A(line) + " " + code.B(line) + " " + code.C(line) + " " + code.Bx(line) + " " + code.sBx(line) + " " + code.codepoint(line));
    }
    for(int line = function.code.length + 1; line <= function.lines.length; line++) {
      if(function.abslineinfo != null && abslineinfoindex < function.abslineinfo.length && function.abslineinfo[abslineinfoindex].pc == line - 1) {
        LAbsLineInfo info = function.abslineinfo[abslineinfoindex++];
        out.println(".abslineinfo\t" + info.pc + "\t" + info.line);
      }
      out.println(".line\t" + function.lines[line - 1]);
    }
    if(function.abslineinfo != null) {
      while(abslineinfoindex < function.abslineinfo.length) {
        LAbsLineInfo info = function.abslineinfo[abslineinfoindex++];
        out.println(".abslineinfo\t" + info.pc + "\t" + info.line);
      }
    }
    out.println();
    
    int subindex = 0;
    for(LFunction child : function.functions) {
      new Disassembler(child, "f" + subindex, fullname).disassemble(out, level + 1, subindex);
      subindex++;
    }
  }
  
}
