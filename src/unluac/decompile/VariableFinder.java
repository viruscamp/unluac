package unluac.decompile;

import java.util.ArrayList;
import java.util.List;

import unluac.parse.LFunction;
import unluac.parse.LUpvalue;

public class VariableFinder {

  static class RegisterState {
    
    public RegisterState() {
      last_written = 1;
      read_count = 0;
      temporary = false;
      local = false;
      read = false;
      written = false;
    }
    
    int last_written;
    int read_count;
    boolean temporary;
    boolean local;
    boolean read;
    boolean written;
  }
  
  static class RegisterStates {
    
    RegisterStates(int registers, int lines) {
      this.registers = registers;
      this.lines = lines;
      states = new RegisterState[lines][registers];
      for(int line = 0; line < lines; line++) {
        for(int register = 0; register < registers; register++) {
          states[line][register] = new RegisterState();
        }
      }
    }
    
    public RegisterState get(int register, int line) {
      return states[line - 1][register];
    }
    
    public void setWritten(int register, int line) {
      get(register, line).written = true;
      get(register, line + 1).last_written = line;
    }
    
    public void setRead(int register, int line) {
      get(register, line).read = true;
      get(register, get(register, line).last_written).read_count++;
    }
    
    public void setLocalRead(int register, int line) {
      for(int r = 0; r <= register; r++) {
        get(r, get(r, line).last_written).local = true;
      }
    }
    
    public void setLocalWrite(int register_min, int register_max, int line) {
      for(int r = 0; r < register_min; r++) {
        get(r, get(r, line).last_written).local = true;
      }
      for(int r = register_min; r <= register_max; r++) {
        get(r, line).local = true;
      }
    }
    
    public void setTemporaryRead(int register, int line) {
      for(int r = register; r < registers; r++) {
        get(r, get(r, line).last_written).temporary = true;
      }
    }
    
    public void setTemporaryWrite(int register_min, int register_max, int line) {
      for(int r = register_max + 1; r < registers; r++) {
        get(r, get(r, line).last_written).temporary = true;
      }
      for(int r = register_min; r <= register_max; r++) {
        get(r, line).temporary = true;
      }
    }
    
    public void nextLine(int line) {
      if(line + 1 < lines) {
        for(int r = 0; r < registers; r++) {
          if(get(r, line).last_written > get(r, line + 1).last_written) {
            get(r, line + 1).last_written = get(r, line).last_written;
          }
        }
      }
    }
    
    private int registers;
    private int lines;
    private RegisterState[][] states;
    
  }
  
  private static boolean isConstantReference(int value) {
    return (value & 0x100) != 0;
  }
  
  public static Declaration[] process(Decompiler d, int args, int registers) {
    Code code = d.code;
    RegisterStates states = new RegisterStates(registers, code.length());
    boolean[] skip = new boolean[code.length()];
    for(int line = 1; line <= code.length(); line++) {
      states.nextLine(line);
      if(skip[line - 1]) continue;
      int A = code.A(line);
      int B = code.B(line);
      int C = code.C(line);
      switch(code.op(line)) {
        case MOVE:
          states.setWritten(A, line);
          states.setRead(B, line);
          if(A < B) {
            states.setLocalWrite(A, A, line);
          } else if(B < A) {
            states.setLocalRead(B, line);
          }
          break;
        case LOADK:
        case LOADBOOL:
        case GETUPVAL:
        case GETGLOBAL:
        case NEWTABLE:
        case NEWTABLE50:
          states.setWritten(A, line);
          break;
        case LOADNIL: {
          int maximum;
          if(d.getVersion().usesOldLoadNilEncoding()) {
            maximum = B;
          } else {
            maximum = A + B;
          }
          int register = code.A(line); 
          while(register <= maximum) {
            states.setWritten(register, line);
            register++;
          }
          break;
        }
        case GETTABLE:
          states.setWritten(A, line);
          if(!isConstantReference(code.B(line))) states.setRead(B, line);
          if(!isConstantReference(code.C(line))) states.setRead(C, line);
          break;
        case SETGLOBAL:
        case SETUPVAL:
          states.setRead(A, line);
          break;
        case SETTABLE:
        case ADD:
        case SUB:
        case MUL:
        case DIV:
        case MOD:
        case POW:
          states.setWritten(A, line);
          if(!isConstantReference(code.B(line))) states.setRead(B, line);
          if(!isConstantReference(code.C(line))) states.setRead(C, line);
          break;
        case SELF:
          states.setWritten(A, line);
          states.setWritten(A + 1, line);
          states.setRead(B, line);
          if(!isConstantReference(code.C(line))) states.setRead(C, line);
          break;
        case UNM:
        case NOT:
        case LEN:
          states.get(code.A(line), line).written = true;
          states.get(code.B(line), line).read = true;
          break;
        case CONCAT:
          states.setWritten(A, line);
          for(int register = B; register <= C; register++) {
            states.setRead(register, line);
            states.setTemporaryRead(register, line);
          }
          break;
        case SETLIST:
          states.setTemporaryRead(A + 1, line);
          break;
        case JMP:
          break;
        case EQ:
        case LT:
        case LE:
          if(!isConstantReference(code.B(line))) states.setRead(B, line);
          if(!isConstantReference(code.C(line))) states.setRead(C, line);
          break;
        case TEST:
          states.setRead(A, line);
          break;
        case TESTSET:
          states.setWritten(A, line);
          states.setLocalWrite(A, A, line);
          states.setRead(B, line);
          break;
        case CLOSURE: {
          LFunction f = d.function.functions[code.Bx(line)];
          for(LUpvalue upvalue : f.upvalues) {
            if(upvalue.instack) {
              states.setLocalRead(upvalue.idx, line);
            }
          }
          break;
        }
        case CALL:
        case TAILCALL: {
          if(code.op(line) != Op.TAILCALL) {
            if(C >= 2) {
              for(int register = A; register <= A + C - 2; register++) {
                states.setWritten(register, line);
              }
            }
          }
          for(int register = A; register <= A + B - 1; register++) {
            states.setRead(register, line);
            states.setTemporaryRead(register, line);
          }
          if(C >= 2) {
            int nline = line + 1;
            int register = A + C - 2;
            while(register >= A && nline <= code.length()) {
              if(code.op(nline) == Op.MOVE && code.B(nline) == register) {
                states.setWritten(code.A(nline), nline);
                states.setRead(code.B(nline), nline);
                states.setLocalWrite(code.A(nline), code.A(nline), nline);
                skip[nline - 1] = true;
              }
              register--;
              nline++;
            }
          }
          break;
        }
      }
    }
    for(int register = 0; register < registers; register++) {
      states.setWritten(register, 1);
    }
    for(int line = 1; line <= code.length(); line++) {
      for(int register = 0; register < registers; register++) {
        RegisterState s = states.get(register, line);
        if(s.written) {
          if(s.read_count >= 2 || (line >= 2 && s.read_count == 0)) {
            states.setLocalWrite(register, register, line);
          }
        }
      }
    }
    for(int line = 1; line <= code.length(); line++) {
      for(int register = 0; register < registers; register++) {
        if(line == 10 && register == 4) {
          System.out.println("here");
        }
        RegisterState s = states.get(register, line);
        if(s.written && s.temporary) {
          List<Integer> ancestors = new ArrayList<Integer>();
          for(int read = 0; read < registers; read++) {
            RegisterState r = states.get(read, line);
            if(r.read && !r.local) {
              ancestors.add(read);
            }
          }
          int pline;
          for(pline = line - 1; pline >= 1; pline--) {
            boolean any_written = false;
            for(int pregister = 0; pregister < registers; pregister++) {
              if(states.get(pregister, pline).written && ancestors.contains(pregister)) {
                any_written = true;
                ancestors.remove((Object)pregister);
              }
            }
            if(!any_written) {
              break;
            }
            for(int pregister = 0; pregister < registers; pregister++) {
              RegisterState a = states.get(pregister, pline); 
              if(a.read && !a.local) {
                ancestors.add(pregister);
              }
            }
          }
          for(int ancestor : ancestors) {
            if(pline >= 1) {
              states.setLocalRead(ancestor, pline);
            }
          }
        }
      }
    }
    for(int register = 0; register < registers; register++) {
      for(int line = 1; line <= code.length(); line++) {
        RegisterState s = states.get(register, line);
        if(s.written || line == 1) {
          System.out.println("WRITE r:" + register + " l:" + line);
          if(s.local) System.out.println("  LOCAL");
          if(s.temporary) System.out.println("  TEMPORARY");
          System.out.println("  READ_COUNT " + s.read_count);
        }
      }
    }
    List<Declaration> declList = new ArrayList<Declaration>(registers); 
    for(int register = 0; register < registers; register++) {
      String id = "L";
      boolean local = false;
      boolean temporary = false;
      int read = 0;
      int written = 0;
      if(register < args) {
        local = true;
        id = "A";
      }
      if(!local && !temporary) {
        for(int line = 1; line <= code.length(); line++) {
          RegisterState state = states.get(register, line);
          if(state.local) local = true;
          if(state.temporary) temporary = true;
          if(state.read) read++;
          if(state.written) written++;
        }
      }
      if(!local && !temporary) {
        if(read >= 2 || read == 0) {
          local = true;
        }
      }
      if(local && temporary) {
        throw new IllegalStateException();
      }
      if(local) {
        Declaration decl = new Declaration(id + register + "_" + lc, 0, code.length() + d.getVersion().getOuterBlockScopeAdjustment());
        decl.register = register;
        lc++;
        declList.add(decl);
      }
    }
    //DEBUG
    //*
    for(Declaration decl : declList) {
      System.out.println("decl: " + decl.name + " " + decl.begin + " " + decl.end);
    }//*/
    return declList.toArray(new Declaration[declList.size()]);
  }
  
  static int lc = 0;
  
  private VariableFinder() {}
  
}
