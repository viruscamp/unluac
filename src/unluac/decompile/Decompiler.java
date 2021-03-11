package unluac.decompile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import unluac.Configuration;
import unluac.Version;
import unluac.decompile.block.Block;
import unluac.decompile.block.DoEndBlock;
import unluac.decompile.expression.ClosureExpression;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;
import unluac.decompile.expression.FunctionCall;
import unluac.decompile.expression.TableLiteral;
import unluac.decompile.expression.TableReference;
import unluac.decompile.expression.Vararg;
import unluac.decompile.operation.CallOperation;
import unluac.decompile.operation.GlobalSet;
import unluac.decompile.operation.LoadNil;
import unluac.decompile.operation.MultipleRegisterSet;
import unluac.decompile.operation.Operation;
import unluac.decompile.operation.RegisterSet;
import unluac.decompile.operation.ReturnOperation;
import unluac.decompile.operation.TableSet;
import unluac.decompile.operation.UpvalueSet;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Label;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.GlobalTarget;
import unluac.decompile.target.TableTarget;
import unluac.decompile.target.Target;
import unluac.decompile.target.UpvalueTarget;
import unluac.decompile.target.VariableTarget;
import unluac.parse.LFunction;
import unluac.util.Stack;

public class Decompiler {
  
  public final LFunction function;
  public final Code code;
  public final Declaration[] declList;
  
  private final int registers;
  private final int length;
  private final Upvalues upvalues;
  
  private final Function f;
  private final LFunction[] functions;  
  private final int params;
  private final int vararg;
  
  public static class State {
    private Registers r;
    private boolean[] skip;
    private Block outer;
    private boolean[] labels;
  }
  
  public Decompiler(LFunction function) {
    this(function, null, -1);
  }
  
  public Decompiler(LFunction function, Declaration[] parentDecls, int line) {
    this.f = new Function(function);
    this.function = function;
    registers = function.maximumStackSize;
    length = function.code.length;
    code = new Code(function);
    if(function.stripped || getConfiguration().variable == Configuration.VariableMode.NODEBUG) {
      if(getConfiguration().variable == Configuration.VariableMode.FINDER) {
        declList = VariableFinder.process(this, function.numParams, function.maximumStackSize);
      } else {
        declList = new Declaration[function.maximumStackSize];
        int scopeEnd = length + function.header.version.outerblockscopeadjustment.get();
        int i;
        for(i = 0; i < Math.min(function.numParams, function.maximumStackSize); i++) {
          declList[i] = new Declaration("A" + i + "_" + function.level, 0, scopeEnd);
        }
        if(getVersion().varargtype.get() != Version.VarArgType.ELLIPSIS && (function.vararg & 1) != 0 && i < function.maximumStackSize) {
          declList[i++] = new Declaration("arg", 0, scopeEnd);
        }
        for(; i < function.maximumStackSize; i++) {
          declList[i] = new Declaration("L" + i + "_" + function.level, 0, scopeEnd);
        }
      }
    } else if(function.locals.length >= function.numParams) {
      declList = new Declaration[function.locals.length];
      for(int i = 0; i < declList.length; i++) {
        declList[i] = new Declaration(function.locals[i], code);
      }
    } else {
      declList = new Declaration[function.numParams];
      for(int i = 0; i < declList.length; i++) {
        declList[i] = new Declaration("_ARG_" + i + "_", 0, length - 1);
      }
    }
    upvalues = new Upvalues(function, parentDecls, line);
    functions = function.functions;
    params = function.numParams;
    vararg = function.vararg;
  }
  
  public Configuration getConfiguration() {
    return function.header.config;
  }
  
  public Version getVersion() {
    return function.header.version;
  }
  
  public boolean getNoDebug() {
    return function.header.config.variable == Configuration.VariableMode.NODEBUG || 
        function.stripped && function.header.config.variable == Configuration.VariableMode.DEFAULT;
  }
  
  public State decompile() {
    State state = new State();
    state.r = new Registers(registers, length, declList, f, getNoDebug());
    ControlFlowHandler.Result result = ControlFlowHandler.process(this, state.r);
    List<Block> blocks = result.blocks;
    state.outer = blocks.get(0);
    state.labels = result.labels;
    processSequence(state, blocks, 1, code.length);
    for(Block block : blocks) {
      block.resolve(state.r);
    }
    handleUnusedConstants(state.outer);
    return state;
  }
  
  public void print(State state) {
    print(state, new Output());
  }
  
  public void print(State state, OutputProvider out) {
    print(state, new Output(out));
  }
  
  public void print(State state, Output out) {
    handleInitialDeclares(out);
    state.outer.print(this, out);
  }
  
  private void handleUnusedConstants(Block outer) {
    Set<Integer> unusedConstants = new HashSet<Integer>(function.constants.length);
    outer.walk(new Walker() {
      
      private int nextConstant = 0;
      
      @Override
      public void visitExpression(Expression expression) {
        if(expression.isConstant()) {
          int index = expression.getConstantIndex();
          if(index >= 0) {
            while(index > nextConstant) {
              unusedConstants.add(nextConstant++);
            }
            if(index == nextConstant) {
              nextConstant++;
            }
          }
        }
      }
      
    });
    outer.walk(new Walker() {
      
      private int nextConstant = 0;
      
      @Override
      public void visitStatement(Statement statement) {
        if(unusedConstants.contains(nextConstant)) {
          if(statement.useConstant(f, nextConstant)) {
            nextConstant++;
          }
        }
      }
      
      @Override
      public void visitExpression(Expression expression) {
        if(expression.isConstant()) {
          int index = expression.getConstantIndex();
          if(index >= nextConstant) {
            nextConstant = index + 1;
          }
        }
      }
      
    });
  }
  
  private void handleInitialDeclares(Output out) {
    List<Declaration> initdecls = new ArrayList<Declaration>(declList.length);
    int initdeclcount = params;
    switch(getVersion().varargtype.get()) {
    case ARG:
    case HYBRID:
      initdeclcount += vararg & 1;
      break;
    case ELLIPSIS:
      break;
    }
    for(int i = initdeclcount; i < declList.length; i++) {
      if(declList[i].begin == 0) {
        initdecls.add(declList[i]);
      }
    }
    if(initdecls.size() > 0) {
      out.print("local ");
      out.print(initdecls.get(0).name);
      for(int i = 1; i < initdecls.size(); i++) {
        out.print(", ");
        out.print(initdecls.get(i).name);
      }
      out.println();
    }
  }
  
  private int fb2int50(int fb) {
    return (fb & 7) << (fb >> 3);
  }
  
  private int fb2int(int fb) {
    int exponent = (fb >> 3) & 0x1f;
    if(exponent == 0) {
      return fb;
    } else {
      return ((fb & 7) + 8) << (exponent - 1);
    }
  }
  
  /**
   * Decodes values from the Lua TMS enumeration used for the MMBIN family of operations.
   */
  private Expression.BinaryOperation decodeBinOp(int tm) {
    switch(tm) {
    case 6: return Expression.BinaryOperation.ADD;
    case 7: return Expression.BinaryOperation.SUB;
    case 8: return Expression.BinaryOperation.MUL;
    case 9: return Expression.BinaryOperation.MOD;
    case 10: return Expression.BinaryOperation.POW;
    case 11: return Expression.BinaryOperation.DIV;
    case 12: return Expression.BinaryOperation.IDIV;
    case 13: return Expression.BinaryOperation.BAND;
    case 14: return Expression.BinaryOperation.BOR;
    case 15: return Expression.BinaryOperation.BXOR;
    case 16: return Expression.BinaryOperation.SHL;
    case 17: return Expression.BinaryOperation.SHR;
    default: throw new IllegalStateException();
    }
  }
  
  private void handle50BinOp(List<Operation> operations, State state, int line, Expression.BinaryOperation op) {
    operations.add(new RegisterSet(line, code.A(line), Expression.make(op, state.r.getKExpression(code.B(line), line), state.r.getKExpression(code.C(line), line))));
  }
  
  private void handle54BinOp(List<Operation> operations, State state, int line, Expression.BinaryOperation op) {
    operations.add(new RegisterSet(line, code.A(line), Expression.make(op, state.r.getExpression(code.B(line), line), state.r.getExpression(code.C(line), line))));
  }
  
  private void handle54BinKOp(List<Operation> operations, State state, int line, Expression.BinaryOperation op) {
    if(line + 1 > code.length || code.op(line + 1) != Op.MMBINK) throw new IllegalStateException();
    Expression left = state.r.getExpression(code.B(line), line);
    Expression right = f.getConstantExpression(code.C(line));
    if(code.k(line + 1)) {
      Expression temp = left;
      left = right;
      right = temp;
    }
    operations.add(new RegisterSet(line, code.A(line), Expression.make(op, left, right)));
  }
  
  private void handleUnaryOp(List<Operation> operations, State state, int line, Expression.UnaryOperation op) {
    operations.add(new RegisterSet(line, code.A(line), Expression.make(op, state.r.getExpression(code.B(line), line))));
  }
  
  private void handleSetList(List<Operation> operations, State state, int line, int stack, int count, int offset) {
    Expression table = state.r.getValue(stack, line);
    for(int i = 1; i <= count; i++) {
      operations.add(new TableSet(line, table, ConstantExpression.createInteger(offset + i), state.r.getExpression(stack + i, line), false, state.r.getUpdated(stack + i, line)));
    }
  }
  
  private List<Operation> processLine(State state, int line) {
    Registers r = state.r;
    boolean[] skip = state.skip;
    List<Operation> operations = new LinkedList<Operation>();
    int A = code.A(line);
    int B = code.B(line);
    int C = code.C(line);
    int Bx = code.Bx(line);
    switch(code.op(line)) {
      case MOVE:
        operations.add(new RegisterSet(line, A, r.getExpression(B, line)));
        break;
      case LOADI:
        operations.add(new RegisterSet(line, A, ConstantExpression.createInteger(code.sBx(line))));
        break;
      case LOADF:
        operations.add(new RegisterSet(line, A, ConstantExpression.createDouble((double) code.sBx(line))));
        break;
      case LOADK:
        operations.add(new RegisterSet(line, A, f.getConstantExpression(Bx)));
        break;
      case LOADKX:
        if(line + 1 > code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
        operations.add(new RegisterSet(line, A, f.getConstantExpression(code.Ax(line + 1))));
        break;
      case LOADBOOL:
        operations.add(new RegisterSet(line, A, ConstantExpression.createBoolean(B != 0)));
        break;
      case LOADFALSE:
      case LFALSESKIP:
        operations.add(new RegisterSet(line, A, ConstantExpression.createBoolean(false)));
        break;
      case LOADTRUE:
        operations.add(new RegisterSet(line, A, ConstantExpression.createBoolean(true)));
        break;
      case LOADNIL:
        operations.add(new LoadNil(line, A, B));
        break;
      case LOADNIL52:
        operations.add(new LoadNil(line, A, A + B));
        break;
      case GETGLOBAL:
        operations.add(new RegisterSet(line, A, f.getGlobalExpression(Bx)));
        break;
      case SETGLOBAL:
        operations.add(new GlobalSet(line, f.getGlobalName(Bx), r.getExpression(A, line)));
        break;
      case GETUPVAL:
        operations.add(new RegisterSet(line, A, upvalues.getExpression(B)));
        break;
      case SETUPVAL:
        operations.add(new UpvalueSet(line, upvalues.getName(B), r.getExpression(A, line)));
        break;
      case GETTABUP:
        operations.add(new RegisterSet(line, A, new TableReference(upvalues.getExpression(B), r.getKExpression(C, line))));
        break;
      case GETTABUP54:
        operations.add(new RegisterSet(line, A, new TableReference(upvalues.getExpression(B), f.getConstantExpression(C))));
        break;
      case GETTABLE:
        operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), r.getKExpression(C, line))));
        break;
      case GETTABLE54:
        operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), r.getExpression(C, line))));
        break;
      case GETI:
        operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), ConstantExpression.createInteger(C))));
        break;
      case GETFIELD:
        operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), f.getConstantExpression(C))));
        break;
      case SETTABLE:
        operations.add(new TableSet(line, r.getExpression(A, line), r.getKExpression(B, line), r.getKExpression(C, line), true, line));
        break;
      case SETTABLE54:
        operations.add(new TableSet(line, r.getExpression(A, line), r.getExpression(B, line), r.getKExpression54(C, code.k(line), line), true, line));
        break;
      case SETI:
        operations.add(new TableSet(line, r.getExpression(A, line), ConstantExpression.createInteger(B), r.getKExpression54(C, code.k(line), line), true, line));
        break;
      case SETFIELD:
        operations.add(new TableSet(line, r.getExpression(A, line), f.getConstantExpression(B), r.getKExpression54(C, code.k(line), line), true, line));
        break;
      case SETTABUP:
        operations.add(new TableSet(line, upvalues.getExpression(A), r.getKExpression(B, line), r.getKExpression(C, line), true, line));
        break;
      case SETTABUP54:
        operations.add(new TableSet(line, upvalues.getExpression(A), f.getConstantExpression(B), r.getKExpression54(C, code.k(line), line), true, line));
        break;
      case NEWTABLE50:
        operations.add(new RegisterSet(line, A, new TableLiteral(fb2int50(B), C == 0 ? 0 : 1 << C)));
        break;
      case NEWTABLE:
        operations.add(new RegisterSet(line, A, new TableLiteral(fb2int(B), fb2int(C))));
        break;
      case NEWTABLE54: {
        if(code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
        int arraySize = C;
        if(code.k(line)) {
          arraySize += code.Ax(line + 1) * (code.getExtractor().C.max() + 1);
        }
        operations.add(new RegisterSet(line, A, new TableLiteral(arraySize, B == 0 ? 0 : (1 << (B - 1)))));
        break;
      }
      case SELF: {
        // We can later determine if : syntax was used by comparing subexpressions with ==
        Expression common = r.getExpression(B, line);
        operations.add(new RegisterSet(line, A + 1, common));
        operations.add(new RegisterSet(line, A, new TableReference(common, r.getKExpression(C, line))));
        break;
      }
      case SELF54: {
        // We can later determine if : syntax was used by comparing subexpressions with ==
        Expression common = r.getExpression(B, line);
        operations.add(new RegisterSet(line, A + 1, common));
        operations.add(new RegisterSet(line, A, new TableReference(common, r.getKExpression54(C, code.k(line), line))));
        break;
      }
      case ADD:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.ADD);
        break;
      case SUB:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.SUB);
        break;
      case MUL:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.MUL);
        break;
      case DIV:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.DIV);
        break;
      case IDIV:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.IDIV);
        break;
      case MOD:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.MOD);
        break;
      case POW:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.POW);
        break;
      case BAND:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.BAND);
        break;
      case BOR:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.BOR);
        break;
      case BXOR:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.BXOR);
        break;
      case SHL:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.SHL);
        break;
      case SHR:
        handle50BinOp(operations, state, line, Expression.BinaryOperation.SHR);
        break;
      case ADD54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.ADD);
        break;
      case SUB54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.SUB);
        break;
      case MUL54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.MUL);
        break;
      case DIV54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.DIV);
        break;
      case IDIV54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.IDIV);
        break;
      case MOD54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.MOD);
        break;
      case POW54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.POW);
        break;
      case BAND54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.BAND);
        break;
      case BOR54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.BOR);
        break;
      case BXOR54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.BXOR);
        break;
      case SHL54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.SHL);
        break;
      case SHR54:
        handle54BinOp(operations, state, line, Expression.BinaryOperation.SHR);
        break;
      case ADDI: {
        if(line + 1 > code.length || code.op(line + 1) != Op.MMBINI) throw new IllegalStateException();
        Expression.BinaryOperation op = decodeBinOp(code.C(line + 1));
        int immediate = code.sC(line);
        boolean swap = false;
        if(code.k(line + 1)) {
          if(op != Expression.BinaryOperation.ADD) {
            throw new IllegalStateException();
          }
          swap = true;
        } else {
          if(op == Expression.BinaryOperation.ADD) {
            // do nothing
          } else if(op == Expression.BinaryOperation.SUB) {
            immediate = -immediate;
          } else {
            throw new IllegalStateException();
          }
        }
        Expression left = r.getExpression(B, line);
        Expression right = ConstantExpression.createInteger(immediate);
        if(swap) {
          Expression temp = left;
          left = right;
          right = temp;
        }
        operations.add(new RegisterSet(line, A, Expression.make(op, left, right)));
        break;
      }
      case ADDK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.ADD);
        break;
      case SUBK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.SUB);
        break;
      case MULK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.MUL);
        break;
      case DIVK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.DIV);
        break;
      case IDIVK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.IDIV);
        break;
      case MODK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.MOD);
        break;
      case POWK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.POW);
        break;
      case BANDK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.BAND);
        break;
      case BORK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.BOR);
        break;
      case BXORK:
        handle54BinKOp(operations, state, line, Expression.BinaryOperation.BXOR);
        break;
      case SHRI: {
        if(line + 1 > code.length || code.op(line + 1) != Op.MMBINI) throw new IllegalStateException();
        int immediate = code.sC(line);
        Expression.BinaryOperation op = decodeBinOp(code.C(line + 1));
        if(op == Expression.BinaryOperation.SHR) {
          // okay
        } else if(op == Expression.BinaryOperation.SHL) {
          immediate = -immediate;
        } else {
          throw new IllegalStateException();
        }
        operations.add(new RegisterSet(line, A, Expression.make(op, r.getExpression(B, line), ConstantExpression.createInteger(immediate))));
        break;
      }
      case SHLI: {
        operations.add(new RegisterSet(line, A, Expression.make(Expression.BinaryOperation.SHL, ConstantExpression.createInteger(code.sC(line)), r.getExpression(B, line))));
        break;
      }
      case MMBIN:
      case MMBINI:
      case MMBINK:
        /* Do nothing ... handled with preceding operation. */
        break;
      case UNM:
        handleUnaryOp(operations, state, line, Expression.UnaryOperation.UNM);
        break;
      case NOT:
        handleUnaryOp(operations, state, line, Expression.UnaryOperation.NOT);
        break;
      case LEN:
        handleUnaryOp(operations, state, line, Expression.UnaryOperation.LEN);
        break;
      case BNOT:
        handleUnaryOp(operations, state, line, Expression.UnaryOperation.BNOT);
        break;
      case CONCAT: {
        Expression value = r.getExpression(C, line);
        //Remember that CONCAT is right associative.
        while(C-- > B) {
          value = Expression.make(Expression.BinaryOperation.CONCAT, r.getExpression(C, line), value);
        }
        operations.add(new RegisterSet(line, A, value));        
        break;
      }
      case CONCAT54: {
        if(B < 2) throw new IllegalStateException();
        B--;
        Expression value = r.getExpression(A + B, line);
        while(B-- > 0) {
          value = Expression.make(Expression.BinaryOperation.CONCAT, r.getExpression(A + B, line), value);
        }
        operations.add(new RegisterSet(line, A, value));        
        break;
      }
      case JMP: case JMP52: case JMP54:
      case EQ: case LT: case LE:
      case EQ54: case LT54: case LE54:
      case EQK: case EQI: case LTI: case LEI: case GTI: case GEI:
      case TEST: case TEST54:
        /* Do nothing ... handled with branches */
        break;
      case TEST50: {
        if(getNoDebug() && A != B) {
          operations.add(new RegisterSet(line, A, Expression.make(Expression.BinaryOperation.OR, r.getExpression(B, line), initialExpression(state, A, line))));
        }
        break;
      }
      case TESTSET: case TESTSET54: {
        if(getNoDebug()) {
          operations.add(new RegisterSet(line, A, Expression.make(Expression.BinaryOperation.OR, r.getExpression(B, line), initialExpression(state, A, line))));
        }
        break;
      }
      case CALL: {
        boolean multiple = (C >= 3 || C == 0);
        if(B == 0) B = registers - A;
        if(C == 0) C = registers - A + 1;
        Expression function = r.getExpression(A, line);
        Expression[] arguments = new Expression[B - 1];
        for(int register = A + 1; register <= A + B - 1; register++) {
          arguments[register - A - 1] = r.getExpression(register, line);
        }
        FunctionCall value = new FunctionCall(function, arguments, multiple);
        if(C == 1) {
          operations.add(new CallOperation(line, value));
        } else {
          if(C == 2 && !multiple) {
            operations.add(new RegisterSet(line, A, value));
          } else {
            operations.add(new MultipleRegisterSet(line, A, A + C - 2, value));
          }
        }
        break;
      }
      case TAILCALL:
      case TAILCALL54: {
        if(B == 0) B = registers - A;
        Expression function = r.getExpression(A, line);
        Expression[] arguments = new Expression[B - 1];
        for(int register = A + 1; register <= A + B - 1; register++) {
          arguments[register - A - 1] = r.getExpression(register, line);
        }
        FunctionCall value = new FunctionCall(function, arguments, true);
        operations.add(new ReturnOperation(line, value));
        skip[line + 1] = true;
        break;
      }
      case RETURN:
      case RETURN54: {
        if(B == 0) B = registers - A + 1;
        Expression[] values = new Expression[B - 1];
        for(int register = A; register <= A + B - 2; register++) {
          values[register - A] = r.getExpression(register, line);
        }
        operations.add(new ReturnOperation(line, values));
        break;
      }
      case RETURN0:
        operations.add(new ReturnOperation(line, new Expression[0]));
        break;
      case RETURN1:
        operations.add(new ReturnOperation(line, new Expression[] {r.getExpression(A, line)}));
        break;
      case FORLOOP: case FORLOOP54:
      case FORPREP: case FORPREP54:
      case TFORPREP: case TFORPREP54:
      case TFORCALL: case TFORCALL54:
      case TFORLOOP: case TFORLOOP52: case TFORLOOP54:
        /* Do nothing ... handled with branches */
        break;
      case SETLIST50: {
        handleSetList(operations, state, line, A, 1 + Bx % 32, Bx - Bx % 32);
        break;
      }
      case SETLISTO: {
        handleSetList(operations, state, line, A, registers - A - 1, Bx - Bx % 32);
        break;
      }
      case SETLIST: {
        if(C == 0) {
          C = code.codepoint(line + 1);
          skip[line + 1] = true;
        }
        if(B == 0) {
          B = registers - A - 1;
        }
        handleSetList(operations, state, line, A, B, (C - 1) * 50);
        break;
      }
      case SETLIST52: {
        if(C == 0) {
          if(line + 1 > code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
          C = code.Ax(line + 1);
          skip[line + 1] = true;
        }
        if(B == 0) {
          B = registers - A - 1;
        }
        handleSetList(operations, state, line, A, B, (C - 1) * 50);
        break;
      }
      case SETLIST54: {
        if(code.k(line)) {
          if(line + 1 > code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
          C += code.Ax(line + 1) * (code.getExtractor().C.max() + 1);
          skip[line + 1] = true;
        }
        if(B == 0) {
          B = registers - A - 1;
        }
        handleSetList(operations, state, line, A, B, C);
        break;
      }
      case TBC:
        r.getDeclaration(A, line).tbc = true;
        break;
      case CLOSE:
        break;
      case CLOSURE: {
        LFunction f = functions[Bx];
        operations.add(new RegisterSet(line, A, new ClosureExpression(f, line + 1)));
        if(function.header.version.upvaluedeclarationtype.get() == Version.UpvalueDeclarationType.INLINE) {
          // Skip upvalue declarations
          for(int i = 0; i < f.numUpvalues; i++) {
            skip[line + 1 + i] = true;
          }
        }
        break;
      }
      case VARARGPREP:
        /* Do nothing ... internal operation */
        break;
      case VARARG: {
        boolean multiple = (B != 2);
        if(B == 1) throw new IllegalStateException();
        if(B == 0) B = registers - A + 1;
        Expression value = new Vararg(B - 1, multiple);
        operations.add(new MultipleRegisterSet(line, A, A + B - 2, value));
        break;
      }
      case VARARG54: {
        boolean multiple = (C != 2);
        if(C == 1) throw new IllegalStateException();
        if(C == 0) C = registers - A + 1;
        Expression value = new Vararg(C - 1, multiple);
        operations.add(new MultipleRegisterSet(line, A, A + C - 2, value));
        break;
      }
      case EXTRAARG:
      case EXTRABYTE:
        /* Do nothing ... handled by previous instruction */
        break;
    }
    return operations;
  }
  
  private Expression initialExpression(State state, int register, int line) {
    if(line == 1) {
      if(register < function.numParams) throw new IllegalStateException();
      return ConstantExpression.createNil(line);
    } else {
      return state.r.getExpression(register, line - 1);
    }
  }
  
  private Assignment processOperation(State state, Operation operation, int line, int nextLine, Block block) {
    Registers r = state.r;
    boolean[] skip = state.skip;
    Assignment assign = null;
    List<Statement> stmts = operation.process(r, block);
    if(stmts.size() == 1) {
      Statement stmt = stmts.get(0);
      if(stmt instanceof Assignment) {
        assign = (Assignment) stmt;
      }
      //System.out.println("-- added statemtent @" + line);
      if(assign != null) {
        boolean declare = false;
        for(Declaration newLocal : r.getNewLocals(line)) {
          if(assign.getFirstTarget().isDeclaration(newLocal)) {
            declare = true;
            break;
          }
        }
        //System.out.println("-- checking for multiassign @" + nextLine);
        while(!declare && nextLine < block.end) {
          Op op = code.op(nextLine);
          if(isMoveIntoTarget(r, nextLine)) {
            //System.out.println("-- found multiassign @" + nextLine);
            Target target = getMoveIntoTargetTarget(r, nextLine, line + 1);
            Expression value = getMoveIntoTargetValue(r, nextLine, line + 1); //updated?
            assign.addFirst(target, value, nextLine);
            skip[nextLine] = true;
            nextLine++;
          } else if(op == Op.MMBIN || op == Op.MMBINI || op == Op.MMBINK) {
            // skip
            nextLine++;
          } else {
            break;
          }
        }
      }
    }
    for(Statement stmt : stmts) {
      block.addStatement(stmt);
    }
    return assign;
  }
  
  public boolean hasStatement(int begin, int end) {
    if(begin <= end) {
      State state = new State();
      state.r = new Registers(registers, length, declList, f, getNoDebug());
      state.outer = new DoEndBlock(function, begin, end + 1);
      state.labels = new boolean[code.length + 1];
      List<Block> blocks = Arrays.asList(state.outer);
      processSequence(state, blocks, begin, end);
      return !state.outer.isEmpty();
    } else {
      return false;
    }
  }
  
  private void processSequence(State state, List<Block> blocks, int begin, int end) {
    Registers r = state.r;
    int blockContainerIndex = 0;
    int blockStatementIndex = 0;
    List<Block> blockContainers = new ArrayList<Block>(blocks.size());
    List<Block> blockStatements = new ArrayList<Block>(blocks.size());
    for(Block block : blocks) {
      if(block.isContainer()) {
        blockContainers.add(block);
      } else {
        blockStatements.add(block);
      }
    }
    Stack<Block> blockStack = new Stack<Block>();
    blockStack.push(blockContainers.get(blockContainerIndex++));
    
    state.skip = new boolean[code.length + 1];
    boolean[] skip = state.skip;
    boolean[] labels_handled = new boolean[code.length + 1];
    
    int line = 1;
    while(true) {
      int nextline = line;
      List<Operation> operations = null;
      List<Declaration> prevLocals = null;
      List<Declaration> newLocals = null;
      
      // Handle container blocks
      if(blockStack.peek().end <= line) {
        Block endingBlock = blockStack.pop();
        Operation operation = endingBlock.process(this);
        if(blockStack.isEmpty()) return;
        if(operation == null) throw new IllegalStateException();
        operations = Arrays.asList(operation);
        prevLocals = r.getNewLocals(line - 1);
      } else {
        if(!labels_handled[line] && state.labels[line]) {
          blockStack.peek().addStatement(new Label(line));
          labels_handled[line] = true;
        }
        
        List<Declaration> locals = r.getNewLocals(line);
        while(blockContainerIndex < blockContainers.size() && blockContainers.get(blockContainerIndex).begin <= line) {
          Block next = blockContainers.get(blockContainerIndex++);
          if(!locals.isEmpty() && next.allowsPreDeclare() && locals.get(0).end > next.scopeEnd()) {
            Assignment declaration = new Assignment();
            int declareEnd = locals.get(0).end;
            declaration.declare(locals.get(0).begin);
            while(!locals.isEmpty() && locals.get(0).end == declareEnd) {
              Declaration decl = locals.get(0);
              declaration.addLast(new VariableTarget(decl), ConstantExpression.createNil(line), line);
              locals.remove(0);
            }
            blockStack.peek().addStatement(declaration);
          }
          blockStack.push(next);
        }
      }
      
      Block block = blockStack.peek();
      
      r.startLine(line);
      
      // Handle other sources of operations (after pushing any new container block)
      if(operations == null) {
        if(blockStatementIndex < blockStatements.size() && blockStatements.get(blockStatementIndex).begin <= line) {
          Block blockStatement = blockStatements.get(blockStatementIndex++);
          Operation operation = blockStatement.process(this);
          operations = Arrays.asList(operation);
        } else {
          // After all blocks are handled for a line, we will reach here
          nextline = line + 1;
          if(!skip[line] && line >= begin && line <= end) {
            operations = processLine(state, line);
          } else {
            operations = Collections.emptyList();
          }
          if(line >= begin && line <= end) {
            newLocals = r.getNewLocals(line);
          }
        }
      }
      
      // Need to capture the assignment (if any) to attach local variable declarations
      Assignment assignment = null;
      
      for(Operation operation : operations) {
        Assignment operationAssignment = processOperation(state, operation, line, nextline, block);
        if(operationAssignment != null) {
          assignment = operationAssignment;
        }
      }
      
      // Some declarations may be swallowed by assignment blocks.
      // These are restored via prevLocals
      List<Declaration> locals = newLocals;
      if(assignment != null && prevLocals != null) {
        locals = prevLocals;
      }
      if(locals != null && !locals.isEmpty()) {
        int scopeEnd = -1;
        if(assignment == null) {
          // Create a new Assignment to hold the declarations
          assignment = new Assignment();
          block.addStatement(assignment);
        } else {
          for(Declaration decl : locals) {
            if(assignment.assigns(decl)) {
              scopeEnd = decl.end;
              break;
            }
          }
        }
          
        assignment.declare(locals.get(0).begin);
        for(Declaration decl : locals) {
          if(scopeEnd == -1 || decl.end == scopeEnd) {
            assignment.addLast(new VariableTarget(decl), r.getValue(decl.register, line + 1), r.getUpdated(decl.register, line - 1));
          }
        }
      }
      
      line = nextline;
    }
  }
  
  private boolean isMoveIntoTarget(Registers r, int line) {
    switch(code.op(line)) {
      case MOVE:
        return r.isAssignable(code.A(line), line) && !r.isLocal(code.B(line), line);
      case SETUPVAL:
      case SETGLOBAL:
        return !r.isLocal(code.A(line), line);
      case SETTABLE:
      case SETTABUP: {
        int C = code.C(line);
        if(f.isConstant(C)) {
          return false;
        } else {
          return !r.isLocal(C, line);
        }
      }
      case SETTABLE54:
      case SETI:
      case SETFIELD:
      case SETTABUP54: {
        if(code.k(line)) {
          return false;
        } else {
          return !r.isLocal(code.C(line), line);
        }
      }
      default:
        return false;
    }
  }
  
  private Target getMoveIntoTargetTarget(Registers r, int line, int previous) {
    switch(code.op(line)) {
      case MOVE:
        return r.getTarget(code.A(line), line);
      case SETUPVAL:
        return new UpvalueTarget(upvalues.getName(code.B(line)));
      case SETGLOBAL:
        return new GlobalTarget(f.getGlobalName(code.Bx(line)));
      case SETTABLE:
        return new TableTarget(r.getExpression(code.A(line), previous), r.getKExpression(code.B(line), previous));
      case SETTABLE54:
        return new TableTarget(r.getExpression(code.A(line), previous), r.getExpression(code.B(line), previous));
      case SETI:
        return new TableTarget(r.getExpression(code.A(line), previous), ConstantExpression.createInteger(code.B(line)));
      case SETFIELD:
        return new TableTarget(r.getExpression(code.A(line), previous), f.getConstantExpression(code.B(line)));
      case SETTABUP: {
        int A = code.A(line);
        int B = code.B(line);
        return new TableTarget(upvalues.getExpression(A), r.getKExpression(B, previous));
      }
      case SETTABUP54: {
        int A = code.A(line);
        int B = code.B(line);
        return new TableTarget(upvalues.getExpression(A), f.getConstantExpression(B));
      }
      default:
        throw new IllegalStateException();
    }
  }
  
  private Expression getMoveIntoTargetValue(Registers r, int line, int previous) {
    int A = code.A(line);
    int B = code.B(line);
    int C = code.C(line);
    switch(code.op(line)) {
      case MOVE:
        return r.getValue(B, previous);
      case SETUPVAL:
      case SETGLOBAL:
        return r.getExpression(A, previous);
      case SETTABLE:
      case SETTABUP:
        if(f.isConstant(C)) {
          throw new IllegalStateException();
        } else {
          return r.getExpression(C, previous);
        }
      case SETTABLE54:
      case SETI:
      case SETFIELD:
      case SETTABUP54:
        if(code.k(line)) {
          throw new IllegalStateException();
        } else {
          return r.getExpression(C, previous);
        }
      default:
        throw new IllegalStateException();
    }
  }
  
}
