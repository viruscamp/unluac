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
  }
  
  public Configuration getConfiguration() {
    return function.header.config;
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
    processSequence();
    for(Block block : blocks) {
      block.resolve(r);
    }
    handleUnusedConstants(outer);
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
      case LOADKX:
        if(line + 1 >= code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
        operations.add(new RegisterSet(line, A, f.getConstantExpression(code.Ax(line + 1))));
        break;
      case LOADBOOL:
        operations.add(new RegisterSet(line, A, new ConstantExpression(new Constant(B != 0 ? LBoolean.LTRUE : LBoolean.LFALSE), -1)));
        break;
      case LOADNIL:
        operations.add(new LoadNil(line, A, B));
        break;
      case LOADNIL52:
        operations.add(new LoadNil(line, A, A + B));
        break;
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
        // We can later determine if : syntax was used by comparing subexpressions with ==
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
      case JMP52:
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
            operations.add(new MultipleRegisterSet(line, A, A + C - 2, value));
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
      case SETLIST:
      case SETLIST52: {
        if(C == 0) {
          if(code.op(line) == Op.SETLIST) {
            C = code.codepoint(line + 1);
          } else {
            if(line + 1 >= code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
            C = code.Ax(line + 1);
          }
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
        operations.add(new RegisterSet(line, A, new ClosureExpression(f, line + 1)));
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
        operations.add(new MultipleRegisterSet(line, A, A + B - 2, value));
        break;
      }
      case EXTRAARG:
      case EXTRABYTE:
        /* Do nothing ... handled by previous instruction */
        break;
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
    Statement stmt = operation.process(r, block);
    if(stmt != null) {
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
        while(!declare && nextLine < block.end && isMoveIntoTarget(nextLine)) {
          //System.out.println("-- found multiassign @" + nextLine);
          Target target = getMoveIntoTargetTarget(nextLine, line + 1);
          Expression value = getMoveIntoTargetValue(nextLine, line + 1); //updated?
          assign.addFirst(target, value);
          skip[nextLine] = true;
          nextLine++;
        }
      }
      
      block.addStatement(stmt);
      
    }
    return assign;
  }
  
  private void processSequence() {
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
    
    skip = new boolean[code.length + 1];
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
        while(blockContainerIndex < blockContainers.size() && blockContainers.get(blockContainerIndex).begin <= line) {
          blockStack.push(blockContainers.get(blockContainerIndex++));
        }
      }
      
      Block block = blockStack.peek();
      
      r.startLine(line);
      
      // Handle other sources of operations (after pushing any new container block)
      if(operations == null) {
        if(blockStack.peek().end <= line) {
          // If the newly pushed block has already ended, don't put anything in it
          operations = Collections.emptyList();
        } else if(blockStatementIndex < blockStatements.size() && blockStatements.get(blockStatementIndex).begin <= line) {
          Block blockStatement = blockStatements.get(blockStatementIndex++);
          Operation operation = blockStatement.process(this);
          operations = Arrays.asList(operation);
        } else {
          // After all blocks are handled for a line, we will reach here
          nextline = line + 1;
          if(!skip[line]) {
            operations = processLine(line);
          } else {
            operations = Collections.emptyList();
          }
          newLocals = r.getNewLocals(line);
        }
      }
      
      // Need to capture the assignment (if any) to attach local variable declarations
      Assignment assignment = null;
      
      for(Operation operation : operations) {
        Assignment operationAssignment = processOperation(operation, line, nextline, block);
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
        if(assignment == null) {
          // Create a new Assignment to hold the declarations
          assignment = new Assignment();
          block.addStatement(assignment);
        }
        assignment.declare(locals.get(0).begin);
        for(Declaration decl : locals) {
          assignment.addLast(new VariableTarget(decl), r.getValue(decl.register, line + 1));
        }
      }
      
      line = nextline;
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
        return new TableTarget(upvalues.getExpression(A), r.getKExpression(B, previous));
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
