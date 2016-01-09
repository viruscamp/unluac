package unluac.decompile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import unluac.Version;
import unluac.decompile.block.Block;
import unluac.decompile.expression.ClosureExpression;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;
import unluac.decompile.expression.FunctionCall;
import unluac.decompile.expression.TableLiteral;
import unluac.decompile.expression.TableReference;
import unluac.decompile.expression.Vararg;
import unluac.decompile.operation.CallOperation;
import unluac.decompile.operation.GlobalSet;
import unluac.decompile.operation.Operation;
import unluac.decompile.operation.RegisterSet;
import unluac.decompile.operation.ReturnOperation;
import unluac.decompile.operation.TableSet;
import unluac.decompile.operation.UpvalueSet;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;
import unluac.decompile.target.GlobalTarget;
import unluac.decompile.target.TableTarget;
import unluac.decompile.target.Target;
import unluac.decompile.target.UpvalueTarget;
import unluac.decompile.target.VariableTarget;
import unluac.parse.LBoolean;
import unluac.parse.LFunction;
import unluac.util.Stack;

public class Decompiler {
  
  private final int registers;
  private final int length;
  public final Code code;
  private final Upvalues upvalues;
  public final Declaration[] declList;
  
  protected Function f;
  protected LFunction function;
  private final LFunction[] functions;  
  private final int params;
  private final int vararg;
  
  private final Op tforTarget;
  private final Op forTarget;
  
  public Decompiler(LFunction function) {
    this(function, null, -1);
  }
  
  public Decompiler(LFunction function, Declaration[] parentDecls, int line) {
    this.f = new Function(function);
    this.function = function;
    registers = function.maximumStackSize;
    length = function.code.length;
    code = new Code(function);
    if(function.stripped) {
      declList = VariableFinder.process(this, function.numParams, function.maximumStackSize);
    } else if(function.locals.length >= function.numParams) {
      declList = new Declaration[function.locals.length];
      for(int i = 0; i < declList.length; i++) {
        declList[i] = new Declaration(function.locals[i]);
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
    tforTarget = function.header.version.getTForTarget();
    forTarget = function.header.version.getForTarget();
  }
  
  public Version getVersion() {
    return function.header.version;
  }
  
  private Registers r;
  private Block outer;
  
  public void decompile() {
    r = new Registers(registers, length, declList, f);
    blocks = new ArrayList<Block>();
    List<Block> myblocks = ControlFlowHandler.process(this, r);
    blocks.addAll(myblocks);
    outer = myblocks.get(0);
    processSequence(1, length);
  }
  
  public void print() {
    print(new Output());
  }
  
  public void print(OutputProvider out) {
    print(new Output(out));
  }
  
  public void print(Output out) {
    handleInitialDeclares(out);
    outer.print(this, out);
  }
  
  private void handleInitialDeclares(Output out) {
    List<Declaration> initdecls = new ArrayList<Declaration>(declList.length);
    for(int i = params + (vararg & 1); i < declList.length; i++) {
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
  
  private List<Operation> processLine(int line) {
    List<Operation> operations = new LinkedList<Operation>();
    int A = code.A(line);
    int B = code.B(line);
    int C = code.C(line);
    int Bx = code.Bx(line);
    switch(code.op(line)) {
      case MOVE:
        operations.add(new RegisterSet(line, A, r.getExpression(B, line)));
        break;
      case LOADK:
        operations.add(new RegisterSet(line, A, f.getConstantExpression(Bx)));
        break;
      case LOADBOOL:
        operations.add(new RegisterSet(line, A, new ConstantExpression(new Constant(B != 0 ? LBoolean.LTRUE : LBoolean.LFALSE), -1)));
        break;
      case LOADNIL: {
        int maximum;
        if(function.header.version.usesOldLoadNilEncoding()) {
          maximum = B;
        } else {
          maximum = A + B;
        }
        while(A <= maximum) {
          operations.add(new RegisterSet(line, A, Expression.NIL));
          A++;
        }
        break;
      }
      case GETUPVAL:
        operations.add(new RegisterSet(line, A, upvalues.getExpression(B)));
        break;
      case GETTABUP:
        operations.add(new RegisterSet(line, A, new TableReference(upvalues.getExpression(B), r.getKExpression(C, line))));
        break;
      case GETGLOBAL:
        operations.add(new RegisterSet(line, A, f.getGlobalExpression(Bx)));
        break;
      case GETTABLE:
        operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), r.getKExpression(C, line))));
        break;
      case SETUPVAL:
        operations.add(new UpvalueSet(line, upvalues.getName(B), r.getExpression(A, line)));
        break;
      case SETTABUP:
        operations.add(new TableSet(line, upvalues.getExpression(A), r.getKExpression(B, line), r.getKExpression(C, line), true, line));
        break;
      case SETGLOBAL:
        operations.add(new GlobalSet(line, f.getGlobalName(Bx), r.getExpression(A, line)));
        break;
      case SETTABLE:
        operations.add(new TableSet(line, r.getExpression(A, line), r.getKExpression(B, line), r.getKExpression(C, line), true, line));
        break;
      case NEWTABLE:
        operations.add(new RegisterSet(line, A, new TableLiteral(fb2int(B), fb2int(C))));
        break;
      case NEWTABLE50:
        operations.add(new RegisterSet(line, A, new TableLiteral(fb2int50(B), 1 << C)));
        break;
      case SELF: {
        // We can later determine is : syntax was used by comparing subexpressions with ==
        Expression common = r.getExpression(B, line);
        operations.add(new RegisterSet(line, A + 1, common));
        operations.add(new RegisterSet(line, A, new TableReference(common, r.getKExpression(C, line))));
        break;
      }
      case ADD:
        operations.add(new RegisterSet(line, A, Expression.makeADD(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case SUB:
        operations.add(new RegisterSet(line, A, Expression.makeSUB(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case MUL:
        operations.add(new RegisterSet(line, A, Expression.makeMUL(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case DIV:
        operations.add(new RegisterSet(line, A, Expression.makeDIV(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case MOD:
        operations.add(new RegisterSet(line, A, Expression.makeMOD(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case POW:
        operations.add(new RegisterSet(line, A, Expression.makePOW(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case IDIV:
        operations.add(new RegisterSet(line, A, Expression.makeIDIV(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case BAND:
        operations.add(new RegisterSet(line, A, Expression.makeBAND(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case BOR:
        operations.add(new RegisterSet(line, A, Expression.makeBOR(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case BXOR:
        operations.add(new RegisterSet(line, A, Expression.makeBXOR(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case SHL:
        operations.add(new RegisterSet(line, A, Expression.makeSHL(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case SHR:
        operations.add(new RegisterSet(line, A, Expression.makeSHR(r.getKExpression(B, line), r.getKExpression(C, line))));
        break;
      case UNM:
        operations.add(new RegisterSet(line, A, Expression.makeUNM(r.getExpression(B, line))));
        break;
      case NOT:
        operations.add(new RegisterSet(line, A, Expression.makeNOT(r.getExpression(B, line))));
        break;
      case LEN:
        operations.add(new RegisterSet(line, A, Expression.makeLEN(r.getExpression(B, line))));
        break;
      case BNOT:
        operations.add(new RegisterSet(line, A, Expression.makeBNOT(r.getExpression(B, line))));
        break;
      case CONCAT: {
        Expression value = r.getExpression(C, line);
        //Remember that CONCAT is right associative.
        while(C-- > B) {
          value = Expression.makeCONCAT(r.getExpression(C, line), value);
        }
        operations.add(new RegisterSet(line, A, value));        
        break;
      }
      case JMP:
      case EQ:
      case LT:
      case LE:
      case TEST:
      case TESTSET:
      case TEST50:
        /* Do nothing ... handled with branches */
        break;
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
            for(int register = A; register <= A + C - 2; register++) {
              operations.add(new RegisterSet(line, register, value));
            }
          }
        }
        break;
      }
      case TAILCALL: {
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
      case RETURN: {
        if(B == 0) B = registers - A + 1;
        Expression[] values = new Expression[B - 1];
        for(int register = A; register <= A + B - 2; register++) {
          values[register - A] = r.getExpression(register, line);
        }
        operations.add(new ReturnOperation(line, values));
        break;
      }
      case FORLOOP:
      case FORPREP:
      case TFORPREP:
      case TFORCALL:
      case TFORLOOP:
        /* Do nothing ... handled with branches */
        break;
      case SETLIST50:
      case SETLISTO: {
        Expression table = r.getValue(A, line);
        int n = Bx % 32;
        for(int i = 1; i <= n + 1; i++) {
          operations.add(new TableSet(line, table, new ConstantExpression(new Constant(Bx - n + i), -1), r.getExpression(A + i, line), false, r.getUpdated(A + i, line)));
        }
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
        Expression table = r.getValue(A, line);
        for(int i = 1; i <= B; i++) {
          operations.add(new TableSet(line, table, new ConstantExpression(new Constant((C - 1) * 50 + i), -1), r.getExpression(A + i, line), false, r.getUpdated(A + i, line)));
        }
        break;
      }
      case CLOSE:
        break;
      case CLOSURE: {
        LFunction f = functions[Bx];
        operations.add(new RegisterSet(line, A, new ClosureExpression(f, declList, line + 1)));
        if(function.header.version.usesInlineUpvalueDeclarations()) {
          // Skip upvalue declarations
          for(int i = 0; i < f.numUpvalues; i++) {
            skip[line + 1 + i] = true;
          }
        }
        break;
      }
      case VARARG: {
        boolean multiple = (B != 2);
        if(B == 1) throw new IllegalStateException();
        if(B == 0) B = registers - A + 1;
        Expression value = new Vararg(B - 1, multiple);
        for(int register = A; register <= A + B - 2; register++) {
          operations.add(new RegisterSet(line, register, value));
        }
        break;
      }
      default:
        throw new IllegalStateException("Illegal instruction: " + code.op(line));
    }
    return operations;
  }
  
  /**
   * When lines are processed out of order, they are noted
   * here so they can be skipped when encountered normally.
   */
  boolean[] skip;

  private Assignment processOperation(Operation operation, int line, int nextLine, Block block) {
    Assignment assign = null;
    boolean wasMultiple = false;
    Statement stmt = operation.process(r, block);
    if(stmt != null) {
      if(stmt instanceof Assignment) {
        assign = (Assignment) stmt;
        if(!assign.getFirstValue().isMultiple()) {
          block.addStatement(stmt);
        } else {
          wasMultiple = true;
        }
      } else {
        block.addStatement(stmt);
      }
      //System.out.println("-- added statemtent @" + line);
      if(assign != null) {
        //System.out.println("-- checking for multiassign @" + nextLine);
        while(nextLine < block.end && isMoveIntoTarget(nextLine)) {
          //System.out.println("-- found multiassign @" + nextLine);
          Target target = getMoveIntoTargetTarget(nextLine, line + 1);
          Expression value = getMoveIntoTargetValue(nextLine, line + 1); //updated?
          assign.addFirst(target, value);
          skip[nextLine] = true;
          nextLine++;
        }
        if(wasMultiple && !assign.getFirstValue().isMultiple()) {
          block.addStatement(stmt);
        }
      }
    }
    return assign;
  }
  
  private void processSequence(int begin, int end) {
    int blockIndex = 1;
    Stack<Block> blockStack = new Stack<Block>();
    blockStack.push(blocks.get(0));
    skip = new boolean[end + 1];
    for(int line = begin; line <= end; line++) {
      /*
      System.out.print("-- line " + line + "; R[0] = ");
      r.getValue(0, line).print(new Output());
      System.out.println();
      System.out.print("-- line " + line + "; R[1] = ");
      r.getValue(1, line).print(new Output());
      System.out.println();
      System.out.print("-- line " + line + "; R[2] = ");
      r.getValue(2, line).print(new Output());
      System.out.println();
      */
      Operation blockHandler = null;
      while(blockStack.peek().end <= line) {
        Block block = blockStack.pop();
        blockHandler = block.process(this);
        if(blockHandler != null) {
          break;
        }
      }
      if(blockHandler == null) {
        while(blockIndex < blocks.size() && blocks.get(blockIndex).begin <= line) {
          blockStack.push(blocks.get(blockIndex++));
        }
      }
      Block block = blockStack.peek();
      ArrayList<Block> blockStatements = new ArrayList<Block>(); 
      while(block != null && !block.isContainer()) {
        blockStack.pop();
        blockStatements.add(block);
        block = blockStack.peek();
      }
      for(Block blockStatement : blockStatements) {
        block.addStatement(blockStatement.process(this).process(r, block));
      }
      r.startLine(line); //Must occur AFTER block.rewrite
      if(skip[line]) {
        List<Declaration> newLocals = r.getNewLocals(line);
        if(!newLocals.isEmpty()) {
          Assignment assign = new Assignment();
          assign.declare(newLocals.get(0).begin);
          for(Declaration decl : newLocals) {
            assign.addLast(new VariableTarget(decl), r.getValue(decl.register, line));
          }
          blockStack.peek().addStatement(assign);
        }
        continue;
      }
      List<Operation> operations = processLine(line);
      List<Declaration> newLocals = r.getNewLocals(blockHandler == null ? line : line - 1);
      //List<Declaration> newLocals = r.getNewLocals(line);
      Assignment assign = null;
      if(blockHandler == null) {
        if(code.op(line) == Op.LOADNIL) {
          assign = new Assignment();
          int count = 0;
          for(Operation operation : operations) {
            RegisterSet set = (RegisterSet) operation;
            operation.process(r, block);
            if(r.isAssignable(set.register, set.line)) {
              assign.addLast(r.getTarget(set.register, set.line), set.value);
              count++;
            }
          }
          if(count > 0) {
            block.addStatement(assign);
          }
        } else {
          //System.out.println("-- Process iterating ... ");
          for(Operation operation : operations) {
            //System.out.println("-- iter");
            Assignment temp = processOperation(operation, line, line + 1, block);
            if(temp != null) {
              assign = temp;
              //System.out.print("-- top assign -> "); temp.getFirstTarget().print(new Output()); System.out.println();
            }
          }
          if(assign != null && assign.getFirstValue().isMultiple()) {
            block.addStatement(assign);
          }
        }
      } else {
        assign = processOperation(blockHandler, line, line, block);
      }
      if(assign != null) {
        if(!newLocals.isEmpty()) {
          assign.declare(newLocals.get(0).begin);
          for(Declaration decl : newLocals) {
            //System.out.println("-- adding decl @" + line);
            assign.addLast(new VariableTarget(decl), r.getValue(decl.register, line + 1));
          }
          //blockStack.peek().addStatement(assign);
        }
      }
      if(blockHandler == null) {
        if(assign != null) {
          
        } else if(!newLocals.isEmpty() && code.op(line) != Op.FORPREP) {
          if(code.op(line) != Op.JMP || code.op(line + 1 + code.sBx(line)) != tforTarget) {
            assign = new Assignment();
            assign.declare(newLocals.get(0).begin);
            for(Declaration decl : newLocals) {
              assign.addLast(new VariableTarget(decl), r.getValue(decl.register, line));
            }
            blockStack.peek().addStatement(assign);
          }
        }
      }
      if(blockHandler != null) {
        //System.out.println("-- repeat @" + line);
        line--;
        continue;
      }
    }    
  }
  
  private boolean isMoveIntoTarget(int line) {
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
      default:
        return false;
    }
  }
  
  private Target getMoveIntoTargetTarget(int line, int previous) {
    switch(code.op(line)) {
      case MOVE:
        return r.getTarget(code.A(line), line);
      case SETUPVAL:
        return new UpvalueTarget(upvalues.getName(code.B(line)));
      case SETGLOBAL:
        return new GlobalTarget(f.getGlobalName(code.Bx(line)));
      case SETTABLE:
        return new TableTarget(r.getExpression(code.A(line), previous), r.getKExpression(code.B(line), previous));
      case SETTABUP: {
        int A = code.A(line);
        int B = code.B(line);
        return new TableTarget(upvalues.getExpression(A), r.getKExpression(B, line));
      }
      default:
        throw new IllegalStateException();
    }
  }
  
  private Expression getMoveIntoTargetValue(int line, int previous) {
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
      default:
        throw new IllegalStateException();
    }
  }
  
  private ArrayList<Block> blocks;
  
}
