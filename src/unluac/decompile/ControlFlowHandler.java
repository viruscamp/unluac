package unluac.decompile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import unluac.Version;
import unluac.decompile.block.AlwaysLoop;
import unluac.decompile.block.Block;
import unluac.decompile.block.Break;
import unluac.decompile.block.DoEndBlock;
import unluac.decompile.block.ForBlock;
import unluac.decompile.block.ElseEndBlock;
import unluac.decompile.block.ForBlock50;
import unluac.decompile.block.ForBlock51;
import unluac.decompile.block.IfThenElseBlock;
import unluac.decompile.block.IfThenEndBlock;
import unluac.decompile.block.OnceLoop;
import unluac.decompile.block.RepeatBlock;
import unluac.decompile.block.SetBlock;
import unluac.decompile.block.TForBlock50;
import unluac.decompile.block.TForBlock51;
import unluac.decompile.block.WhileBlock;
import unluac.decompile.block.OuterBlock;
import unluac.decompile.block.TForBlock;
import unluac.decompile.condition.AndCondition;
import unluac.decompile.condition.BinaryCondition;
import unluac.decompile.condition.Condition;
import unluac.decompile.condition.ConstantCondition;
import unluac.decompile.condition.OrCondition;
import unluac.decompile.condition.RegisterSetCondition;
import unluac.decompile.condition.SetCondition;
import unluac.decompile.condition.TestCondition;
import unluac.parse.LFunction;
import unluac.util.Stack;

public class ControlFlowHandler {
  
  public static boolean verbose = false;
  
  private static class Branch implements Comparable<Branch> {
    
    private static enum Type {
      comparison,
      test,
      testset,
      finalset,
      jump;
    }
    
    public Branch previous;
    public Branch next;
    public int line;
    public int target;
    public Type type;
    public Condition cond;
    public int targetFirst;
    public int targetSecond;
    public boolean inverseValue;
    
    public Branch(int line, Type type, Condition cond, int targetFirst, int targetSecond) {
      this.line = line;
      this.type = type;
      this.cond = cond;
      this.targetFirst = targetFirst;
      this.targetSecond = targetSecond;
      this.inverseValue = false;
      this.target = -1;
    }

    @Override
    public int compareTo(Branch other) {
      return this.line - other.line;
    }
  }
  
  private static class State {
    public Decompiler d;
    public LFunction function;
    public Registers r;
    public Code code;
    public Branch begin_branch;
    public Branch end_branch;
    public Branch[] branches;
    public Branch[] setbranches;
    public Branch[] finalsetbranches;
    public boolean[] reverse_targets;
    public int[] resolved;
    public List<Block> blocks;
  }
  
  public static List<Block> process(Decompiler d, Registers r) {
    State state = new State();
    state.d = d;
    state.function = d.function;
    state.r = r;
    state.code = d.code;
    find_reverse_targets(state);
    find_branches(state);
    combine_branches(state);
    resolve_lines(state);
    initialize_blocks(state);
    find_fixed_blocks(state);
    find_while_loops(state);
    find_repeat_loops(state);
    //find_break_statements(state);
    //find_if_blocks(state, d.declList);
    find_if_break(state, d.declList);
    //find_other_statements(state, d.declList);
    find_set_blocks(state);
    find_pseudo_goto_statements(state, d.declList);
    find_do_blocks(state, d.declList);
    Collections.sort(state.blocks);
    // DEBUG: print branches stuff
    /*
    Branch b = state.begin_branch;
    while(b != null) {
      System.out.println("Branch at " + b.line);
      System.out.println("\tcondition: " + b.cond);
      b = b.next;
    }
    */
    return state.blocks;
  }
  
  private static void find_reverse_targets(State state) {
    Code code = state.code;
    boolean[] reverse_targets = state.reverse_targets = new boolean[state.code.length + 1];
    for(int line = 1; line <= code.length; line++) {
      if(is_jmp(state, line)) {
        int target = code.target(line);
        if(target <= line) {
          reverse_targets[target] = true;
        }
      }
    }
  }
  
  private static void resolve_lines(State state) {
    int[] resolved = new int[state.code.length + 1];
    Arrays.fill(resolved, -1);
    for(int line = 1; line <= state.code.length; line++) {
      int r = line;
      Branch b = state.branches[line];
      while(b != null && b.type == Branch.Type.jump) {
        if(resolved[r] >= 1) {
          r = resolved[r];
          break;
        } else if(resolved[r] == -2) {
          r = b.targetSecond;
          break;
        } else {
          resolved[r] = -2;
          r = b.targetSecond;
          b = state.branches[r];
        }
      }
      if(r == line && state.code.op(line) == Op.JMP52 && is_close(state, line)) {
        r = line + 1;
      }
      resolved[line] = r;
    }
    state.resolved = resolved;
  }
  
  private static int find_loadboolblock(State state, int target) {
    int loadboolblock = -1;
    if(state.code.op(target) == Op.LOADBOOL) {
      if(state.code.C(target) != 0) {
        loadboolblock = target;
      } else if(target - 1 >= 1 && state.code.op(target - 1) == Op.LOADBOOL && state.code.C(target - 1) != 0) {
        loadboolblock = target - 1;
      }
    }
    return loadboolblock;
  }
  
  private static void handle_loadboolblock(State state, boolean[] skip, int loadboolblock, Condition c, int line, int target) {
    int loadboolvalue = state.code.B(target);
    int final_line = -1;
    if(loadboolblock - 1 >= 1 && is_jmp(state, loadboolblock - 1)) {
      int boolskip_target = state.code.target(loadboolblock - 1);
      int boolskip_target_redirected = -1;
      if(is_jmp_raw(state, loadboolblock + 2)) {
        boolskip_target_redirected = state.code.target(loadboolblock + 2);
      }
      if(boolskip_target == loadboolblock + 2 || boolskip_target == boolskip_target_redirected) {
        skip[loadboolblock - 1] = true;
        final_line = loadboolblock - 2;
      }
    }
    boolean inverse = false;
    if(loadboolvalue == 1) {
      inverse = true;
      c = c.inverse();
    }
    boolean constant = is_jmp(state, line);
    Branch b;
    int begin = line + 2;
    
    if(constant) {
      begin--;
      b = new Branch(line, Branch.Type.testset, c, begin, loadboolblock + 2);
    } else if(line + 2 == loadboolblock) {
      b = new Branch(line, Branch.Type.finalset, c, begin, loadboolblock + 2);
    } else {
      b = new Branch(line, Branch.Type.testset, c, begin, loadboolblock + 2);
    }
    b.target = state.code.A(loadboolblock);
    b.inverseValue = inverse;
    insert_branch(state, b);
    
    if(final_line != -1)
    {
      if(constant && final_line < begin && state.finalsetbranches[final_line + 1] == null) {
        c = new TestCondition(final_line + 1, state.code.A(target));
        b = new Branch(final_line + 1, Branch.Type.finalset, c, final_line, loadboolblock + 2);
        b.target = state.code.A(loadboolblock);
        insert_branch(state, b);
      }
      if(final_line >= begin && state.finalsetbranches[final_line] == null) {
        c = new SetCondition(final_line, get_target(state, final_line));
        b = new Branch(final_line, Branch.Type.finalset, c, final_line, loadboolblock + 2);
        b.target = state.code.A(loadboolblock);
        insert_branch(state, b);
      }
      if(final_line + 1 == begin && state.finalsetbranches[final_line + 1] == null) {
        c = new RegisterSetCondition(loadboolblock, get_target(state, loadboolblock));
        b = new Branch(final_line + 1, Branch.Type.finalset, c, final_line, loadboolblock + 2);
        b.target = state.code.A(loadboolblock);
        insert_branch(state, b);
      }
    }
  }
  
  private static void handle_test(State state, boolean[] skip, int line, Condition c, int target, boolean constant) {
    Code code = state.code;
    int loadboolblock = find_loadboolblock(state, target);
    if(loadboolblock >= 1) {
      if(!constant && code.C(line) != 0) c = c.inverse();
      handle_loadboolblock(state, skip, loadboolblock, c, line, target);
    } else {
      int ploadboolblock = !constant && target - 2 >= 1 ? find_loadboolblock(state, target - 2) : -1;
      if(ploadboolblock != -1 && ploadboolblock == target - 2 && code.A(target - 2) == c.register() && !has_statement(state, line + 2, target - 3)) {
        handle_testset(state, skip, line, c, target, c.register());
      } else {
        if(!constant && code.C(line) != 0) c = c.inverse();
        Branch b = new Branch(line, constant ? Branch.Type.testset : Branch.Type.test, c, line + 2, target);
        b.target = code.A(line);
        if(code.C(line) != 0) b.inverseValue = true;
        insert_branch(state, b);
      }
    }
    skip[line + 1] = true;
  }
  
  private static void handle_testset(State state, boolean[] skip, int line, Condition c, int target, int register) {
    Code code = state.code;
    Branch b = new Branch(line, Branch.Type.testset, c, line + 2, target);
    b.target = register;
    if(code.C(line) != 0) b.inverseValue = true;
    skip[line + 1] = true;
    insert_branch(state, b);
    int final_line = target - 1;
    if(state.finalsetbranches[final_line] == null) {
      int loadboolblock = find_loadboolblock(state, target - 2);
      if(loadboolblock == -1) {
        c = null;
        if(line + 2 == target) {
          c = new RegisterSetCondition(line, get_target(state, line));
          final_line = final_line + 1;
        } else if(code.op(final_line) != Op.JMP && code.op(final_line) != Op.JMP52) {
          c = new SetCondition(final_line, get_target(state, final_line));
        }
        if(c != null) {
          b = new Branch(final_line, Branch.Type.finalset, c, target, target);
          b.target = register;
          insert_branch(state, b);
        }
      }
    }
  }
  
  private static void find_branches(State state) {
    Code code = state.code;
    state.branches = new Branch[state.code.length + 1];
    state.setbranches = new Branch[state.code.length + 1];
    state.finalsetbranches = new Branch[state.code.length + 1];
    boolean[] skip = new boolean[code.length + 1];
    for(int line = 1; line <= code.length; line++) {
      if(!skip[line]) {
        switch(code.op(line)) {
          case EQ:
          case LT:
          case LE: {
            BinaryCondition.Operator op = BinaryCondition.Operator.EQ;
            if(code.op(line) == Op.LT) op = BinaryCondition.Operator.LT;
            if(code.op(line) == Op.LE) op = BinaryCondition.Operator.LE;
            int left = code.B(line);
            int right = code.C(line);
            int target = code.target(line + 1);
            Condition c = new BinaryCondition(op, line, left, right);
            if(code.A(line) == 1) {
              c = c.inverse();
            }
            int loadboolblock = find_loadboolblock(state, target);
            if(loadboolblock >= 1) {
              handle_loadboolblock(state, skip, loadboolblock, c, line, target);
            } else {
              Branch b = new Branch(line, Branch.Type.comparison, c, line + 2, target);
              if(code.A(line) == 1) {
                b.inverseValue = true;
              }
              insert_branch(state, b);
            }
            skip[line + 1] = true;
            break;
          }
          case TEST50: {
            Condition c = new TestCondition(line, code.B(line));
            int target = code.target(line + 1);
            if(code.A(line) == code.B(line)) {
              handle_test(state, skip, line, c, target, false);
            } else {
              handle_testset(state, skip, line, c, target, code.A(line));
            }
            break;
          }
          case TEST: {
            Condition c;
            boolean constant = false;
            int target = code.target(line + 1);
            if(line - 1 >= 1 && code.op(line - 1) == Op.LOADBOOL && code.A(line - 1) == code.A(line) && code.C(line - 1) == 0) {
              if(target <= code.length && target - 2 >= 1 && code.op(target - 2) == Op.LOADBOOL && code.C(target - 2) != 0) {
                constant = true;
              }
            }
            c = new TestCondition(line, code.A(line));
            handle_test(state, skip, line, c, target, constant);
            break;
          }
          case TESTSET: {
            Condition c = new TestCondition(line, code.B(line));
            int target = code.target(line + 1);
            handle_testset(state, skip, line, c, target, code.A(line));
            break;
          }
          case JMP:
          case JMP52: {
            if(is_jmp(state, line)) {
              int target = code.target(line);
              int loadboolblock = find_loadboolblock(state, target);
              if(loadboolblock >= 1) {
                handle_loadboolblock(state, skip, loadboolblock, new ConstantCondition(-1, false), line, target);
              } else {
                Branch b = new Branch(line, Branch.Type.jump, null, target, target);
                insert_branch(state, b);
              }
            }
            break;
          }
          default:
            break;
        }
      }
    }
    link_branches(state);
  }
  
  private static void combine_branches(State state) {
    Branch b;
    
    b = state.end_branch;
    while(b != null) {
      b = combine_left(state, b).previous;
    }
  }
  
  private static void initialize_blocks(State state) {
    state.blocks = new LinkedList<Block>();
  }
  
  private static void find_fixed_blocks(State state) {
    List<Block> blocks = state.blocks;
    Registers r = state.r;
    Code code = state.code;
    Op tforTarget = state.function.header.version.getTForTarget();
    Op forTarget = state.function.header.version.getForTarget();
    blocks.add(new OuterBlock(state.function, state.code.length));
    
    boolean[] loop = new boolean[state.code.length + 1];
    
    Branch b = state.begin_branch;
    while(b != null) {
      if(b.type == Branch.Type.jump) {
        int line = b.line;
        int target = b.targetFirst;
        if(code.op(target) == tforTarget && !loop[target]) {
          loop[target] = true;
          int A = code.A(target);
          int C = code.C(target);
          if(C == 0) throw new IllegalStateException();
          remove_branch(state, state.branches[line]);
          if(state.branches[target + 1] != null) {
            remove_branch(state, state.branches[target + 1]);
          }
          
          boolean forvarClose = false;
          boolean innerClose = false;
          int close = target - 1;
          if(close >= line + 1 && is_close(state, close) && code.A(close) == A + 3) {
            forvarClose = true;
            close--;
          }
          if(close >= line + 1 && is_close(state, close) && code.A(close) <= A + 3 + C) {
            innerClose = true;
          }
          
          TForBlock block = new TForBlock51(state.function, line + 1, target + 2, A, C, forvarClose, innerClose);
          block.handleVariableDeclarations(r);
          blocks.add(block);
        } else if(code.op(target) == forTarget && !loop[target]) {
          loop[target] = true;
          int A = code.A(target);
          
          boolean innerClose = false;
          int close = target - 1;
          if(close >= line + 1 && is_close(state, close) && code.A(close) == A + 3) {
            innerClose = true;
          }
          
          ForBlock block = new ForBlock50(state.function, line + 1, target + 1, A, innerClose);
          block.handleVariableDeclarations(r);
          
          blocks.add(block);
          remove_branch(state, b);
        }
      }
      b = b.next;
    }
    
    for(int line = 1; line <= code.length; line++) {
      switch(code.op(line)) {
        case FORPREP: {
          
          int A = code.A(line);
          int target = code.target(line);
          
          boolean forvarClose = false;
          boolean innerClose = false;
          int close = target - 1;
          if(close >= line + 1 && is_close(state, close) && code.A(close) == A + 3) {
            forvarClose = true;
            close--;
          }
          if(close >= line + 1 && is_close(state, close) && code.A(close) <= A + 4) {
            innerClose = true;
          }
          
          ForBlock block = new ForBlock51(state.function, line + 1, target + 1, A, forvarClose, innerClose);
          block.handleVariableDeclarations(r);
          blocks.add(block);
          break;
        }
        case TFORPREP: {
          int target = code.target(line);
          int A = code.A(target);
          int C = code.C(target);
          
          boolean innerClose = false;
          int close = target - 1;
          if(close >= line + 1 && is_close(state, close) && code.A(close) == A + 3 + C) {
            innerClose = true;
          }
          
          TForBlock block = new TForBlock50(state.function, line + 1, target + 2, A, C, innerClose);
          block.handleVariableDeclarations(r);
          blocks.add(block);
          remove_branch(state, state.branches[target + 1]);
          break;
        }
        default:
          break;
      }
    }
  }
  
  private static void unredirect(State state, int begin, int end, int line, int target) {
    Branch b = state.begin_branch;
    while(b != null) {
      if(b.line >= begin && b.line < end && b.targetSecond == target) {
        b.targetSecond = line;
        if(b.targetFirst == target) {
          b.targetFirst = line;
        }
      }
      b = b.next;
    }
  }
  
  private static void find_while_loops(State state) {
    List<Block> blocks = state.blocks;
    Branch j = state.end_branch;
    while(j != null) {
      if(j.type == Branch.Type.jump && j.targetFirst <= j.line) {
        int line = j.targetFirst;
        int loopback = line;
        int end = j.line + 1;
        Branch b = state.begin_branch;
        while(b != null) {
          if(is_conditional(b) && b.line >= loopback && b.line < j.line && state.resolved[b.targetSecond] == state.resolved[end]) {
            break;
          }
          b = b.next;
        }
        if(b != null) {
          boolean reverse = state.reverse_targets[loopback];
          state.reverse_targets[loopback] = false;
          if(has_statement(state, loopback, b.line - 1)) {
            b = null;
          }
          state.reverse_targets[loopback] = reverse;
        }
        if(state.function.header.version == Version.LUA50) {
          b = null; // while loop aren't this style
        }
        Block loop;
        if(b != null) {
          b.targetSecond = end;
          remove_branch(state, b);
          //System.err.println("while " + b.targetFirst + " " + b.targetSecond);
          loop = new WhileBlock(state.function, b.cond, b.targetFirst, b.targetSecond, loopback);
          unredirect(state, loopback, end, j.line, loopback);
        } else {
          boolean repeat = false;
          if(state.function.header.version == Version.LUA50) {
            repeat = true;
            if(loopback - 1 >= 1 && state.branches[loopback - 1] != null) {
              Branch head = state.branches[loopback - 1];
              if(head.type == Branch.Type.jump && head.targetFirst == j.line) {
                remove_branch(state, head);
                repeat = false;
              }
            }
          }
          loop = new AlwaysLoop(state.function, loopback, end, repeat);
          unredirect(state, loopback, end, j.line, loopback);
        }
        remove_branch(state, j);
        blocks.add(loop);
      }
      j = j.previous;
    }
  }
  
  private static void find_repeat_loops(State state) {
    List<Block> blocks = state.blocks;
    Branch b = state.begin_branch;
    while(b != null) {
      if(is_conditional(b)) {
        if(b.targetSecond < b.targetFirst) {
          Block block = null;
          if(state.function.header.version == Version.LUA50) {
            int head = b.targetSecond - 1;
            if(head >= 1 && state.branches[head] != null && state.branches[head].type == Branch.Type.jump) {
              Branch headb = state.branches[head];
              if(headb.targetSecond <= b.line) {
                if(has_statement(state, headb.targetSecond, b.line - 1)) {
                  headb = null;
                }
                if(headb != null) {
                  block = new WhileBlock(state.function, b.cond.inverse(), head + 1, b.targetFirst, -1);
                  remove_branch(state, headb);
                  unredirect(state, 1, headb.line, headb.line, headb.targetSecond);
                }
              }
            }
          }
          if(block == null) {
            block = new RepeatBlock(state.function, b.cond, b.targetSecond, b.targetFirst);
          }
          remove_branch(state, b);
          blocks.add(block);
        }
      }
      b = b.next;
    }
  }
  
  private static boolean splits_decl(int begin, int end, Declaration[] declList) {
    for(Declaration decl : declList) {
      if(decl.isSplitBy(begin, end)) {
        return true;
      }
    }
    return false;
  }
  
  private static int stack_reach(State state, Stack<Branch> stack) {
    for(int i = 0; i < stack.size(); i++) {
      Branch b = stack.peek(i);
      Block breakable = enclosing_breakable_block(state, b.line);
      if(breakable != null && breakable.end == b.targetSecond) {
        // next
      } else {
        return b.targetSecond;
      }
    }
    return Integer.MAX_VALUE;
  }
  
  private static void resolve_if_stack(State state, Stack<Branch> stack, int line, int count) {
    while(!stack.isEmpty() && stack_reach(state, stack) <= line && count != 0) {
      if(count > 0) count--;
      Branch top = stack.pop();
      Block breakable = enclosing_breakable_block(state, top.line);
      if(breakable != null && breakable.end == top.targetSecond) {
        // 5.2-style if-break
        Block block = new IfThenEndBlock(state.function, state.r, top.cond.inverse(), top.targetFirst - 1, top.targetFirst - 1, false);
        block.addStatement(new Break(state.function, top.targetFirst - 1, top.targetSecond));
        state.blocks.add(block);
        throw new IllegalStateException();
      } else {
        int literalEnd = state.code.target(top.targetFirst - 1);
        state.blocks.add(new IfThenEndBlock(state.function, state.r, top.cond, top.targetFirst, top.targetSecond, literalEnd != top.targetSecond));
      }
      remove_branch(state, top);
    }
    if(count > 0) {
      throw new IllegalStateException();
    }
  }
  
  private static void resolve_else(State state, Stack<Branch> stack, Stack<Branch> hanging, Stack<ElseEndBlock> elseStack, Branch top, Branch b, int tailTargetSecond) {
    while(!elseStack.isEmpty() && elseStack.peek().end == tailTargetSecond && elseStack.peek().begin >= top.targetFirst) {
      elseStack.pop().end = b.line;
    }
    Stack<Branch> replace = new Stack<Branch>();
    while(!hanging.isEmpty() && hanging.peek().targetSecond == tailTargetSecond) {
      Branch hanger = hanging.pop();
      hanger.targetSecond = b.line;
      Block breakable = enclosing_breakable_block(state, hanger.line);
      if(breakable != null && hanger.targetSecond >= breakable.end) {
        replace.push(hanger);
      } else {
        stack.push(hanger);
        resolve_if_stack(state, stack, b.line, 1);
      }
    }
    while(!replace.isEmpty()) {
      hanging.push(replace.pop());
    }
    b.targetSecond = tailTargetSecond;
    state.blocks.add(new IfThenElseBlock(state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond));
    ElseEndBlock elseBlock = new ElseEndBlock(state.function, top.targetSecond, b.targetSecond);
    state.blocks.add(elseBlock);
    elseStack.push(elseBlock);
    remove_branch(state, b);
  }
  
  private static void resolve_hangers(State state, Stack<Branch> stack, Stack<Branch> hanging, Branch b) {
    if(b != null) {
      Block enclosing = enclosing_block(state, b.line);
      while(!hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst && enclosing_block(state, hanging.peek().line) == enclosing) {
        Branch hanger = hanging.pop();
        hanger.targetSecond = b.line;
        stack.push(hanger);
        resolve_if_stack(state, stack, b.line, 1);
      }
    }
  }
  
  private static void find_if_break(State state, Declaration[] declList) {
    Stack<Branch> stack = new Stack<Branch>();
    Stack<Branch> hanging = new Stack<Branch>();
    Stack<ElseEndBlock> elseStack = new Stack<ElseEndBlock>();
    Branch b = state.begin_branch;
    Branch hangingResolver = null;
    
    while(b != null) {
      resolve_if_stack(state, stack, b.line, -1);
      while(!elseStack.isEmpty() && elseStack.peek().end <= b.line) {
        elseStack.pop();
      }
      
      if(is_conditional(b)) {
        Block enclosing = enclosing_unprotected_block(state, b.line);
        if(b.targetFirst > b.targetSecond) throw new IllegalStateException();
        if(enclosing != null && !enclosing.contains(b.targetSecond)) {
          if(b.targetSecond == enclosing.getUnprotectedTarget()) {
            b.targetSecond = enclosing.getUnprotectedLine();
          }
        }
        Block breakable = enclosing_breakable_block(state, b.line);
        if(!stack.isEmpty() && stack.peek().targetSecond < b.targetSecond) {
          hanging.push(b);
        } else if(breakable != null && b.targetSecond >= breakable.end) {
          resolve_hangers(state, stack, hanging, hangingResolver);
          hangingResolver = null;
          hanging.push(b);
        } else {
          stack.push(b);
        }
      } else if(b.type == Branch.Type.jump) {
        int line = b.line;
        
        Block enclosing = enclosing_block(state, b.line);
        
        int tailTargetSecond = b.targetSecond;
        Block unprotected = enclosing_unprotected_block(state, b.line);
        if(unprotected != null && !unprotected.contains(b.targetSecond)) {
          if(tailTargetSecond == state.resolved[unprotected.getUnprotectedTarget()]) {
            tailTargetSecond = unprotected.getUnprotectedLine();
          }             
        }
        
        Block breakable = enclosing_breakable_block(state, line);
        if(breakable != null && (b.targetFirst == breakable.end || b.targetFirst == state.resolved[breakable.end])) {
          Break block = new Break(state.function, b.line, b.targetFirst);
          if(!hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst && enclosing_block(state, hanging.peek().line) == enclosing) {
            if(hangingResolver != null && hangingResolver.targetFirst != b.targetFirst) {
              resolve_hangers(state, stack, hanging, hangingResolver);
            }
            hangingResolver = b;
          }
          state.blocks.add(block);
          remove_branch(state, b);
        } else if(!stack.isEmpty() && stack.peek().targetSecond - 1 == b.line) {
          Branch top = stack.peek();
          while(top != null && top.targetSecond - 1 == b.line && splits_decl(top.targetFirst, top.targetSecond, declList)) {
            resolve_if_stack(state, stack, top.targetSecond, 1);
            top = stack.peek();
          }
          if(top != null && top.targetSecond - 1 == b.line) {
            if(top.targetSecond != b.targetSecond) {
              resolve_hangers(state, stack, hanging, hangingResolver);
              hangingResolver = null;
              resolve_else(state, stack, hanging, elseStack, top, b, tailTargetSecond);
              stack.pop();
            } else if(!splits_decl(top.targetFirst, top.targetSecond - 1, declList)) {
              // "empty else" case
              b.targetSecond = tailTargetSecond;
              state.blocks.add(new IfThenElseBlock(state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond));
              remove_branch(state, b);
              stack.pop();
            }
          }
        } else if(
          breakable != null
          && !hanging.isEmpty() && state.resolved[hanging.peek().targetSecond] == state.resolved[breakable.end]
          && line + 1 < state.branches.length && state.branches[line + 1].type == Branch.Type.jump
          && state.branches[line + 1].targetFirst == hanging.peek().targetSecond
        ) {
          // else break
          hangingResolver = null;
          Branch top = hanging.pop();
          top.targetSecond = line + 1;
          resolve_else(state, stack, hanging, elseStack, top, b, tailTargetSecond);
        } else if(
          !stack.isEmpty() && stack.peek().targetSecond == b.targetFirst
          && line + 1 < state.branches.length && state.branches[line + 1].type == Branch.Type.jump
          && state.branches[line + 1].targetFirst == b.targetFirst
        ) {
          // empty else (redirected)
          Branch top = stack.peek();
          if(!splits_decl(top.targetFirst, b.line, declList)) {
            top.targetSecond = line + 1;
            b.targetSecond = line + 1;
            state.blocks.add(new IfThenElseBlock(state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond));
            remove_branch(state, b);
            stack.pop();
          }
        } else if(
          !hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst
          && line + 1 < state.branches.length && state.branches[line + 1].type == Branch.Type.jump
          && state.branches[line + 1].targetFirst == b.targetFirst
        ) {
          // empty else (redirected)
          Branch top = hanging.peek();
          if(!splits_decl(top.targetFirst, b.line, declList)) {
            hangingResolver = null;
            top.targetSecond = line + 1;
            b.targetSecond = line + 1;
            state.blocks.add(new IfThenElseBlock(state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond));
            remove_branch(state, b);
            hanging.pop();
          }
        }
      }
      b = b.next;
    }
    resolve_hangers(state, stack, hanging, hangingResolver);
    hangingResolver = null;
    while(!hanging.isEmpty()) {
      if(state.d.getVersion().usesIfBreakRewrite()) {
        // if break (or if goto)
        // TODO: handle if goto
        Branch top = hanging.pop();
        Block breakable = enclosing_breakable_block(state, top.line);
        if(breakable != null && breakable.end == top.targetSecond) {
          Block block = new IfThenEndBlock(state.function, state.r, top.cond.inverse(), top.targetFirst - 1, top.targetFirst - 1, false);
          block.addStatement(new Break(state.function, top.targetFirst - 1, top.targetSecond));
          state.blocks.add(block);
          
        }  else {
          throw new IllegalStateException("IF-GOTO");
        }
        remove_branch(state, top);
      } else {
        throw new IllegalStateException();
      }
    }
    resolve_if_stack(state, stack, Integer.MAX_VALUE, -1);
  }
  
  private static void find_if_blocks(State state, Declaration[] declList) {
    Branch b = state.begin_branch;
    while(b != null) {
      if(is_conditional(b)) {
        Block enclosing;
        enclosing = enclosing_unprotected_block(state, b.line);
        if(enclosing != null && !enclosing.contains(b.targetSecond)) {
          if(b.targetSecond == enclosing.getUnprotectedTarget()) {
            b.targetSecond = enclosing.getUnprotectedLine();
          }
        }
        boolean isElse = false;
        Branch tail = b.targetSecond >= 1 ? state.branches[b.targetSecond - 1] : null;
        if(tail != null && !is_conditional(tail) && !splits_decl(b.targetFirst, b.targetSecond, declList)) {
          int tailTargetSecond = tail.targetSecond;
          enclosing = enclosing_unprotected_block(state, tail.line);
          if(enclosing != null && !enclosing.contains(tail.targetSecond)) {
            if(tailTargetSecond == state.resolved[enclosing.getUnprotectedTarget()]) {
              tailTargetSecond = enclosing.getUnprotectedLine();
            }             
          }
          //System.err.println("else end " + b.targetFirst + " " + b.targetSecond + " " + tail.targetSecond + " enclosing " + (enclosing != null ? enclosing.begin : -1) + " " + + (enclosing != null ? enclosing.end : -1));
          
          if(b.targetSecond != tail.targetSecond) {
            tail.targetSecond = tailTargetSecond;
            state.blocks.add(new IfThenElseBlock(state.function, b.cond, b.targetFirst, b.targetSecond, tail.targetSecond));
            state.blocks.add(new ElseEndBlock(state.function, b.targetSecond, tail.targetSecond));
            remove_branch(state, tail);
            unredirect(state, b.targetFirst, b.targetSecond, b.targetSecond - 1, tail.targetSecond);
            isElse = true;
          } else if(!splits_decl(b.targetFirst, b.targetSecond - 1, declList)){
            // "empty else" case
            tail.targetSecond = tailTargetSecond;
            state.blocks.add(new IfThenElseBlock(state.function, b.cond, b.targetFirst, b.targetSecond, tail.targetSecond));
            remove_branch(state, tail);
            unredirect(state, b.targetFirst, b.targetSecond, b.targetSecond - 1, tail.targetSecond);
            isElse = true;
          }
        }
        if(!isElse) {
          //System.err.println("if end " + b.targetFirst + " " + b.targetSecond);
          
          Block breakable = enclosing_breakable_block(state, b.line);
          if(breakable != null && breakable.end == b.targetSecond) {
            // 5.2-style if-break
            Block block = new IfThenEndBlock(state.function, state.r, b.cond.inverse(), b.targetFirst - 1, b.targetFirst - 1, false);
            block.addStatement(new Break(state.function, b.targetFirst - 1, b.targetSecond));
            state.blocks.add(block);
          } else {
            int literalEnd = state.code.target(b.targetFirst - 1);
            state.blocks.add(new IfThenEndBlock(state.function, state.r, b.cond, b.targetFirst, b.targetSecond, literalEnd != b.targetSecond));
          }
        }
        
        remove_branch(state, b);
      }
      b = b.next;
    }
  }
 
  private static void find_set_blocks(State state) {
    List<Block> blocks = state.blocks;
    Branch b = state.begin_branch;
    while(b != null) {
      if(is_assignment(b) || b.type == Branch.Type.finalset) {
        Block block = new SetBlock(state.function, b.cond, b.target, b.line, b.targetFirst, b.targetSecond, state.r);
        blocks.add(block);
        remove_branch(state, b);
      }
      b = b.next;
    }
  }
  
  private static Block enclosing_block(State state, int line) {
    Block enclosing = null;
    for(Block block : state.blocks) {
      if(block.contains(line)) {
        if(enclosing == null || enclosing.contains(block)) {
          enclosing = block;
        }
      }
    }
    return enclosing;
  }
  
  private static Block enclosing_breakable_block(State state, int line) {
    Block enclosing = null;
    for(Block block : state.blocks) {
      if(block.contains(line) && block.breakable()) {
        if(enclosing == null || enclosing.contains(block)) {
          enclosing = block;
        }
      }
    }
    return enclosing;
  }
  
  private static Block enclosing_unprotected_block(State state, int line) {
    Block enclosing = null;
    for(Block block : state.blocks) {
      if(block.contains(line) && block.isUnprotected()) {
        if(enclosing == null || enclosing.contains(block)) {
          enclosing = block;
        }
      }
    }
    return enclosing;
  }
  
  private static void unredirect_break(State state, int line, Block enclosing) {
    Branch b = state.end_branch;
    while(b != null) {
      Block breakable = enclosing_breakable_block(state, b.line);
      if(b.line < line && breakable != null && (b.type == Branch.Type.jump || is_conditional(b) && state.function.header.version.usesIfBreakRewrite()) && breakable == enclosing && state.resolved[b.targetSecond] == state.resolved[enclosing.end]) {
        //System.err.println("redirect break " + b.line + " from " + b.targetFirst + " to " + line);
        boolean condsplit = false;
        Branch c = state.begin_branch;
        while(c != null) {
          if(is_conditional(c) && c.targetSecond < breakable.end) {
            if(b != c && c.targetFirst <= line && line < c.targetSecond) {
              if(c.targetFirst <= b.line && b.line < c.targetSecond) {
                
              } else {
                condsplit = true;
                break;
              }
            }
            if(b != c && c.targetFirst <= b.line && b.line < c.targetSecond - 1 || c.targetFirst == b.line) {
              if(c.targetFirst <= line && line < c.targetSecond) {
                
              } else {
                condsplit = true;
                break;
              }
            }
          }
          if(b != c && c.line != line && b.type == Branch.Type.jump && c.type == Branch.Type.jump && c.targetSecond < breakable.end) {
            if(b.line < c.line && c.targetFirst > line)
            {
              condsplit = true;
              break;
            }
          }
          c = c.next;
        }
        if(!condsplit) {
          if(b.targetFirst == enclosing.end) {
            b.targetFirst = line;
          }
          if(state.resolved[b.targetSecond] == state.resolved[enclosing.end]) {
            b.targetSecond = line;
          }
        }
      }
      b = b.previous;
    }
  }
  
  private static class ResolutionState {
    ResolutionState(State state, Block container) {
      this.container = container;
      resolution = new BranchResolution[state.code.length + 1];
      results = new ArrayList<ResolutionResult>();
      blocks = new ResolutionBlocks();
      for(Declaration decl : state.d.declList) {
        if(container == null || (container.contains(decl.begin) && decl.end <= container.scopeEnd())) {
          blocks.addDecl(decl);
        }
      }
    }
    
    Block container;
    BranchResolution[] resolution;
    List<ResolutionResult> results;
    ResolutionBlocks blocks;
  }
  
  private static class BranchResolution {
    
    enum Type {
      IF_END,
      IF_ELSE,
      IF_BREAK,
      ELSE,
      BREAK,
      PSEUDO_GOTO,
    };
    
    Type type;
    int line;
    boolean matched;
    int earliestMatch;
    
  }
  
  private static class ResolutionBlocks {
    
    ResolutionBlocks() {
      blocks = new ArrayList<Pair>();
      decls = new ArrayList<Pair>();
    }
    
    enum Type
    {
      PSEUDO_GOTO,
      IF_END,
      IF_ELSE,
      ELSE_END,
      IF_ELSE_END;
    }
    
    boolean push(int begin, int end, Type type) {
      for(Pair b : blocks) {
        if(end <= b.begin) {
          // okay
        } else if(b.end <= begin) {
          // okay
        } else if(begin <= b.begin && b.end <= end) {
          // okay
        } else if(b.begin <= begin && end <= b.end) {
          // okay
        } else {
          if(debug_resolution) System.err.println("early invalid overlap");
          return false;
        }
      }
      if(type == Type.IF_END) {
        for(Pair d : decls) {
          if(end - 1 <= d.begin) {
            // okay
          } else if(d.end <= begin) {
            // okay
          } else if(begin <= d.begin && d.end <= end - 1) {
            // okay
          } else if(d.begin <= begin && end - 1 <= d.end) {
            // okay
          } else {
            if(debug_resolution) System.err.println("early invalid scope overlap");
            return false;
          }
        }
      } else if(type == Type.IF_ELSE) {
        for(Pair d : decls) {
          if(end <= d.begin) {
            // okay
          } else if(d.end <= begin) {
            // okay
          } else if(begin <= d.begin && d.end <= end) {
            // okay
          } else if(d.begin <= begin && end <= d.end) {
            // okay
          } else {
            if(debug_resolution) System.err.println("early invalid scope overlap");
            return false;
          }
        }
      }
      blocks.add(new Pair(begin, end));
      return true;
    }
    
    void addDecl(Declaration decl) {
      decls.add(new Pair(decl.begin, decl.end));
    }
    
    int save() {
      return blocks.size();
    }
    
    void restore(int size) {
      while(blocks.size() > size) {
        blocks.remove(blocks.size() - 1);
      }
    }
    
    List<Pair> blocks;
    List<Pair> decls;
  }
  
  private static class Pair {
    
    Pair(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }
    
    int begin;
    int end;
  }
  
  private static class ResolutionResult {
    
    List<Block> blocks = new ArrayList<Block>();
    
  }
  
  private static ResolutionResult finishResolution(State state, Declaration[] declList, ResolutionState rstate) {
    ResolutionResult result = new ResolutionResult();
    for(int i = 0; i < rstate.resolution.length; i++) {
      BranchResolution r = rstate.resolution[i];
      if(r != null) {
        Branch b = state.branches[i];
        if(b == null) throw new IllegalStateException();
        switch(r.type) {
        case ELSE:
          break;
        case BREAK:
          result.blocks.add(new Break(state.function, b.line, r.line));
          break;
        case PSEUDO_GOTO:
          // handled in second pass
          break;
        case IF_END:
          int literalEnd = state.code.target(b.targetFirst - 1);
          result.blocks.add(new IfThenEndBlock(state.function, state.r, b.cond, b.targetFirst, r.line, literalEnd != r.line));
          break;
        case IF_ELSE:
          BranchResolution r_else = rstate.resolution[r.line - 1];
          if(r_else == null) throw new IllegalStateException();
          result.blocks.add(new IfThenElseBlock(state.function, b.cond, b.targetFirst, r.line, r_else.line));
          if(r.line != r_else.line) {
            result.blocks.add(new ElseEndBlock(state.function, r.line, r_else.line));
          } // else "empty else" case
          break;
        case IF_BREAK:
          Block block = new IfThenEndBlock(state.function, state.r, b.cond.inverse(), b.targetFirst - 1, b.targetFirst - 1, false);
          block.addStatement(new Break(state.function, b.targetFirst - 1, r.line));
          result.blocks.add(block);
          break;
        default:
          throw new IllegalStateException();
        }
      }
    }
    for(int i = 0; i < rstate.resolution.length; i++) {
      BranchResolution r = rstate.resolution[i];
      if(r != null) {
        Branch b = state.branches[i];
        if(b == null) throw new IllegalStateException();
        if(r.type == BranchResolution.Type.PSEUDO_GOTO) {
          Block smallest = rstate.container;
          if(smallest == null) smallest = state.blocks.get(0); // outer block TODO cleaner way to get
          for(Block newblock : result.blocks) {
            if(smallest.contains(newblock) && newblock.contains(b.line) && newblock.contains(r.line - 1)) {
              smallest = newblock;
            }
          }
          Block wrapping = null;
          for(Block block : result.blocks) {
            if(block != smallest && smallest.contains(block) && block.contains(b.line)) {
              if(wrapping == null || block.contains(wrapping)) {
                wrapping = block;
              }
            }
          }
          for(Block block : state.blocks) {
            if(block != smallest && smallest.contains(block) && block.contains(b.line)) {
              if(wrapping == null || block.contains(wrapping)) {
                wrapping = block;
              }
            }
          }
          int begin;
          if(wrapping != null) {
            begin = wrapping.begin - 1;
          } else {
            begin = b.line;
          }
          
          for(Declaration decl : declList) {
            if(decl.begin >= begin && decl.begin < r.line) {
              
            }
            if(decl.end >= begin && decl.end < r.line) {
              if(decl.begin < begin) {
                begin = decl.begin;
              }
            }
          }
          
          result.blocks.add(new OnceLoop(state.function, begin, r.line));
          result.blocks.add(new Break(state.function, b.line, r.line));
        }
      }
    }
    return result;
  }
  
  private static int findEarliestIfElseLine(State state, ResolutionState rstate, Branch b) {
    int earliest = Integer.MAX_VALUE;
    Branch p = state.end_branch;
    while(p != null) {
      if(is_conditional(p) && enclosing_breakable_block(state, p.line) == rstate.container) {
        if(p.targetSecond - 1 == b.line) {
          earliest = Math.min(earliest, p.line);
        }
      } else if(p.type == Branch.Type.jump && enclosing_breakable_block(state, p.line) == rstate.container && p != b) {
        if(p.line - 1 == b.line) {
          Branch p2 = state.end_branch;
          while(p2 != null) {
            if(is_conditional(p2) && enclosing_breakable_block(state, p2.line) == rstate.container) {
              if(p.targetFirst == p2.targetSecond) {
                earliest = Math.min(earliest, p2.line);
              }
            }
            p2 = p2.previous;
          }
        }
      }
      p = p.previous;
    }
    return earliest;
  }
  
  private static boolean debug_resolution = false;
  
  private static boolean checkResolution(State state, ResolutionState rstate) {
    List<Pair> blocks = new ArrayList<Pair>();
    List<Pair> pseudoGotos = new ArrayList<Pair>();
    for(int i = 0; i < rstate.resolution.length; i++) {
      BranchResolution r = rstate.resolution[i];
      if(r != null) {
        Branch b = state.branches[i];
        if(b == null) throw new IllegalStateException();
        switch(r.type) {
        case ELSE:
          if(!r.matched) {
            if(debug_resolution) System.err.println("unmatched else");
            return false;
          }
          if(rstate.container != null && r.line >= rstate.container.end) {
            if(debug_resolution) System.err.println("invalid else");
            return false;
          }
          break;
        case BREAK:
          if(rstate.container == null || r.line < rstate.container.end) {
            if(debug_resolution) System.err.println("invalid break");
            return false;
          }
          break;
        case PSEUDO_GOTO:
          if(rstate.container != null && r.line >= rstate.container.end) {
            if(debug_resolution) System.err.println("invalid pseudo goto");
            return false;
          }
          pseudoGotos.add(new Pair(b.line, r.line));
          break;
        case IF_END:
          if(rstate.container != null && r.line >= rstate.container.end) {
            if(debug_resolution) System.err.println("invalid if end");
            return false;
          }
          blocks.add(new Pair(b.line + 1, r.line));
          break;
        case IF_ELSE:
          if(rstate.container != null && r.line >= rstate.container.end) {
            if(debug_resolution) System.err.println("invalid if else");
            return false;
          }
          BranchResolution r_else = rstate.resolution[r.line - 1];
          if(r_else == null) throw new IllegalStateException();
          blocks.add(new Pair(b.line + 1, r.line - 1));
          blocks.add(new Pair(r.line, r_else.line));
          blocks.add(new Pair(b.line + 1, r_else.line));
          break;
        case IF_BREAK:
          if(rstate.container == null || r.line < rstate.container.end) {
            if(debug_resolution) System.err.println("invalid if break");
            return false;
          }
          break;
        default:
          throw new IllegalStateException();
        }
      }
    }
    for(int i = 0; i < blocks.size(); i++) {
      for(int j = i + 1; j < blocks.size(); j++) {
        Pair block1 = blocks.get(i);
        Pair block2 = blocks.get(j);
        if(block1.end <= block2.begin) {
          // okay
        } else if(block2.end <= block1.begin) {
          // okay
        } else if(block1.begin <= block2.begin && block2.end <= block1.end) {
          // okay
        } else if(block2.begin <= block1.begin && block1.end <= block2.end) {
          // okay
        } else {
          if(debug_resolution) {
            System.err.println("invalid block overlap");
            //System.err.println("  block1: " + block1.begin + " " + block1.end);
            //System.err.println("  block2: " + block2.begin + " " + block2.end);
          }
          return false;
        }
      }
    }
    for(Pair pseudoGoto : pseudoGotos) {
      for(Pair block : blocks) {
        if(block.begin <= pseudoGoto.end && block.end > pseudoGoto.end) {
          // block contains end
          if(block.begin > pseudoGoto.begin || block.end <= pseudoGoto.begin) {
            // doesn't contain goto
            if(debug_resolution) System.err.println("invalid pseudo goto block overlap");
            return false;
          }
        }
      }
    }
    for(int i = 0; i < pseudoGotos.size(); i++) {
      for(int j = 0; j < pseudoGotos.size(); j++) {
        Pair goto1 = pseudoGotos.get(i);
        Pair goto2 = pseudoGotos.get(j);
        if(goto1.begin >= goto2.begin && goto1.begin < goto2.end) {
          if(debug_resolution) System.err.println("invalid pseudo goto overlap");
          if(goto1.end > goto2.end) return false;
        }
        if(goto2.begin >= goto1.begin && goto2.begin < goto1.end) {
          if(debug_resolution) System.err.println("invalid pseudo goto overlap");
          if(goto2.end > goto1.end) return false;
        }
      }
    }
    // TODO: check for break out of OnceBlock
    return true;
  }
  
  private static void printResolution(State state, ResolutionState rstate) {
    for(int i = 0; i < rstate.resolution.length; i++) {
      BranchResolution r = rstate.resolution[i];
      if(r != null) {
        Branch b = state.branches[i];
        if(b == null) throw new IllegalStateException();
        System.err.print(r.type + " " + b.line + " " + r.line);
        if(b.cond != null) System.err.print(" " + b.cond);
        System.err.println();
      }
    }
  }
  
  private static boolean is_break(State state, Block container, int line) {
    if(container == null || line < container.end) return false;
    if(line == container.end) return true;
    return state.resolved[container.end] == state.resolved[line];
  }
  
  private static void resolve(State state, Declaration[] declList, ResolutionState rstate, Branch b) {
    if(b == null) {
      if(checkResolution(state, rstate)) {
        // printResolution(state, rstate);
        // System.err.println();
        rstate.results.add(finishResolution(state, declList, rstate));
      } else {
        // System.err.println("failed resolution:");
        // printResolution(state, rstate);
      }
      return;
    }
    // fail fast
    for(int i = 0; i < rstate.resolution.length; i++) {
      BranchResolution res = rstate.resolution[i];
      if(res != null && res.type == BranchResolution.Type.ELSE && !res.matched && res.earliestMatch > b.line) {
        return;
      }
    }
    // end fail fast
    Branch next = b.previous;
    while(next != null && enclosing_breakable_block(state, next.line) != rstate.container) {
      next = next.previous;
    }
    if(is_conditional(b)) {
      BranchResolution r = new BranchResolution();
      rstate.resolution[b.line] = r;
      if(is_break(state, rstate.container, b.targetSecond)) {
        if(state.function.header.version.usesIfBreakRewrite()) {
          r.type = BranchResolution.Type.IF_BREAK;
          r.line = rstate.container.end;
          resolve(state, declList, rstate, next);
          if(!rstate.results.isEmpty()) return;
        }
      }
      BranchResolution prevlineres = null;
      r.line = b.targetSecond;
      if(b.targetSecond - 1 >= 1) {
        prevlineres = rstate.resolution[b.targetSecond - 1];
      }
      if(prevlineres != null && prevlineres.type == BranchResolution.Type.ELSE && !prevlineres.matched) {
        r.type = BranchResolution.Type.IF_ELSE;
        prevlineres.matched = true;
        if(b.line < prevlineres.earliestMatch) throw new IllegalStateException("unexpected else match: " + b.line + " (" + prevlineres.earliestMatch + ")");
        int blocksSize = rstate.blocks.save();
        if(rstate.blocks.push(b.line + 1, r.line - 1, ResolutionBlocks.Type.IF_ELSE) && rstate.blocks.push(r.line, prevlineres.line, ResolutionBlocks.Type.ELSE_END) && rstate.blocks.push(b.line + 1,  prevlineres.line, ResolutionBlocks.Type.IF_ELSE_END)) {
          resolve(state, declList, rstate, next);
        }
        rstate.blocks.restore(blocksSize);;
        if(!rstate.results.isEmpty()) return;
        prevlineres.matched = false;
      }
      if(b.targetSecond - 1 >= 1 && state.code.op(b.targetSecond - 1) == Op.JMP52 && is_close(state, b.targetSecond - 1)) {
        r.line--;
        r.type = BranchResolution.Type.IF_END;
        int blocksSize = rstate.blocks.save();
        if(rstate.blocks.push(b.line + 1, r.line, ResolutionBlocks.Type.IF_END)) {
          resolve(state, declList, rstate, next);
        }
        rstate.blocks.restore(blocksSize);
        if(!rstate.results.isEmpty()) return;
        r.line++;
      }
      r.type = BranchResolution.Type.IF_END;
      int blocksSize = rstate.blocks.save();
      if(rstate.blocks.push(b.line + 1, r.line, ResolutionBlocks.Type.IF_END)) {
        resolve(state, declList, rstate, next);
      }
      rstate.blocks.restore(blocksSize);
      if(!rstate.results.isEmpty()) return;
      Branch p = state.end_branch;
      while(p != b) {
        if(p.type == Branch.Type.jump && enclosing_breakable_block(state, p.line) == rstate.container) {
          if(p.targetFirst == b.targetSecond) {
            r.line = p.line;
            if(p.line - 1 >= 1) {
              prevlineres = rstate.resolution[p.line - 1];
            }
            if(prevlineres != null && prevlineres.type == BranchResolution.Type.ELSE && !prevlineres.matched) {
              r.type = BranchResolution.Type.IF_ELSE;
              prevlineres.matched = true;
              if(b.line < prevlineres.earliestMatch) throw new IllegalStateException("unexpected else match: " + b.line + " (" + prevlineres.earliestMatch + "); " + p.line);
              resolve(state, declList, rstate, next);
              if(!rstate.results.isEmpty()) return;
              prevlineres.matched = false;
            }
            r.type = BranchResolution.Type.IF_END;
            blocksSize = rstate.blocks.save();
            if(rstate.blocks.push(b.line + 1, r.line, ResolutionBlocks.Type.IF_END)) {
              resolve(state, declList, rstate, next);
            }
            rstate.blocks.restore(blocksSize);
            if(!rstate.results.isEmpty()) return;
          }
        }
        p = p.previous;
      }
      rstate.resolution[b.line] = null;
    } else if(b.type == Branch.Type.jump) {
      BranchResolution r = new BranchResolution();
      rstate.resolution[b.line] = r;
      if(is_break(state, rstate.container, b.targetFirst)) {
        r.type = BranchResolution.Type.BREAK;
        r.line = rstate.container.end;
        resolve(state, declList, rstate, next);
        if(!rstate.results.isEmpty()) return;
      }
      r.type = BranchResolution.Type.ELSE;
      r.line = b.targetFirst;
      r.earliestMatch = findEarliestIfElseLine(state, rstate, b);
      resolve(state, declList, rstate, next);
      if(!rstate.results.isEmpty()) return;
      Branch p = state.end_branch;
      while(p != b) {
        if(p.type == Branch.Type.jump && enclosing_breakable_block(state, p.line) == rstate.container) {
          if(p.targetFirst == b.targetFirst) {
            r.type = BranchResolution.Type.ELSE;
            r.line = p.line;
            r.earliestMatch = findEarliestIfElseLine(state, rstate, b);
            resolve(state, declList, rstate, next);
            if(!rstate.results.isEmpty()) return;
          }
        }
        p = p.previous;
      }
      r.type = BranchResolution.Type.PSEUDO_GOTO;
      resolve(state, declList, rstate, next);
      if(!rstate.results.isEmpty()) return;
      rstate.resolution[b.line] = null;
    } else {
      resolve(state, declList, rstate, next);
      if(!rstate.results.isEmpty()) return;
    }
  }
  
  private static void find_other_statements(State state, Declaration[] declList) {
    List<Block> containers = new ArrayList<Block>();
    for(Block block : state.blocks) {
      if(block.breakable()) {
        containers.add(block);
      }
    }
    containers.add(null);
    
    for(Block container : containers) {
      Branch b = state.end_branch;
      while(b != null && enclosing_breakable_block(state, b.line) != container) {
        b = b.previous;
      }
      
      //System.out.println("resolve " + (container == null ? 0 : container.begin));
      ResolutionState rstate = new ResolutionState(state, container);
      resolve(state, declList, rstate, b);
      if(rstate.results.isEmpty()) throw new IllegalStateException("couldn't resolve breaks for " + (container == null ? 0 : container.begin));
      state.blocks.addAll(rstate.results.get(0).blocks);
    }
  }
  
  private static void find_break_statements(State state) {
    List<Block> blocks = state.blocks;
    Branch b = state.end_branch;
    LinkedList<Branch> breaks = new LinkedList<Branch>();
    while(b != null) {
      if(b.type == Branch.Type.jump) {
        int line = b.line;
        Block enclosing = enclosing_breakable_block(state, line);
        if(enclosing != null && (b.targetFirst == enclosing.end || b.targetFirst == state.resolved[enclosing.end])) {
          Break block = new Break(state.function, b.line, b.targetFirst);
          unredirect_break(state, line, enclosing);
          blocks.add(block);
          breaks.addFirst(b);
        }
      }
      b = b.previous;
    }
    
    b = state.begin_branch;
    List<Branch> ifStack = new ArrayList<Branch>(); 
    while(b != null) {
      Block enclosing = enclosing_breakable_block(state, b.line);
      while(!ifStack.isEmpty()) {
        Block outer = enclosing_breakable_block(state, ifStack.get(ifStack.size() - 1).line); 
        if(enclosing == null || (outer != enclosing && !outer.contains(enclosing))) {
          ifStack.remove(ifStack.size() - 1);
        } else {
          break;
        }
      }
      if(is_conditional(b)) {
        //System.err.println("conditional " + b.line + " " + b.targetSecond);
        if(enclosing != null && b.targetSecond >= enclosing.end) {
          ifStack.add(b);
        }
      } else if(b.type == Branch.Type.jump) {
        //System.err.println("lingering jump " + b.line);
        if(enclosing != null && b.targetFirst < enclosing.end && !ifStack.isEmpty()) {
          if(b.line <= state.code.length - 1 && state.branches[b.line + 1] != null) {
            Branch prev = state.branches[b.line + 1];
            if(prev.type == Branch.Type.jump && (prev.targetFirst == enclosing.end || prev.targetFirst == state.resolved[enclosing.end])) {
              Branch candidate = ifStack.get(ifStack.size() - 1);
              if(state.resolved[candidate.targetSecond] == state.resolved[prev.targetFirst]) {
                candidate.targetSecond = prev.line;
                ifStack.remove(ifStack.size() - 1);
              }
            }
          }
        }
      }
      b = b.next;
    }
    
    b = state.begin_branch;
    while(b != null) {
      if(is_conditional(b)) {
        Block enclosing = enclosing_breakable_block(state, b.line);
        if(enclosing != null && (b.targetSecond >= enclosing.end || b.targetSecond < enclosing.begin)) {
          if(state.function.header.version.usesIfBreakRewrite()) {
            Block block = new IfThenEndBlock(state.function, state.r, b.cond.inverse(), b.targetFirst - 1, b.targetFirst - 1, false);
            block.addStatement(new Break(state.function, b.targetFirst - 1, b.targetSecond));
            state.blocks.add(block);
            remove_branch(state, b);
          } else {
            for(Branch br : breaks) {
              if(br.line >= b.targetFirst && br.line < b.targetSecond && br.line < enclosing.end) {
                Branch tbr = br;
                while(b.targetSecond != tbr.targetSecond) {
                  Branch next = state.branches[tbr.targetSecond];
                  if(next != null && next.type == Branch.Type.jump) {
                    tbr = next; // TODO: guard against infinite loop
                  } else {
                    break;
                  }
                }
                if(b.targetSecond == tbr.targetSecond) {
                  b.targetSecond = br.line;
                }
              }
            }
          }
        }
      }
      b = b.next;
    }
    
    for(Branch br : breaks) {
      remove_branch(state, br);
    }
  }
  
  private static void find_pseudo_goto_statements(State state, Declaration[] declList) {
    Branch b = state.begin_branch;
    while(b != null) {
      if(b.type == Branch.Type.jump && b.targetFirst > b.line) {
        int end = b.targetFirst;
        Block smallestEnclosing = null;
        for(Block block : state.blocks) {
          if(block.contains(b.line) && block.contains(end - 1)) {
            if(smallestEnclosing == null || smallestEnclosing.contains(block)) {
              smallestEnclosing = block;
            }
          }
        }
        if(smallestEnclosing != null) {
          // Should always find the outer block at least...
          Block wrapping = null;
          for(Block block : state.blocks) {
            if(block != smallestEnclosing && smallestEnclosing.contains(block) && block.contains(b.line)) {
              if(wrapping == null || block.contains(wrapping)) {
                wrapping = block;
              }
            }
          }
          int begin = smallestEnclosing.begin;
          if(wrapping != null) {
            begin = Math.max(wrapping.begin - 1, smallestEnclosing.begin);
            //beginMax = begin;
          }
          int lowerBound = Integer.MIN_VALUE;
          int upperBound = Integer.MAX_VALUE;
          final int scopeAdjust = -1;
          for(Declaration decl : declList) {
            //if(decl.begin >= begin && decl.begin < end) {
              
            //}
            if(decl.end >= begin && decl.end <= end + scopeAdjust) {
              if(decl.begin < begin) {
                upperBound = Math.min(decl.begin, upperBound);
              }
            }
            if(decl.begin >= begin && decl.begin <= end + scopeAdjust && decl.end > end + scopeAdjust) {
              lowerBound = Math.max(decl.begin + 1, lowerBound);
              begin = decl.begin + 1;
            }
          }
          if(lowerBound > upperBound) {
            throw new IllegalStateException();
          }
          begin = Math.max(lowerBound, begin);
          begin = Math.min(upperBound, begin);
          Block breakable = enclosing_breakable_block(state, b.line);
          if(breakable != null) {
            begin = Math.max(breakable.begin, begin);
          }
          state.blocks.add(new OnceLoop(state.function, begin, end));
          Break breakStatement = new Break(state.function, b.line, b.targetFirst);
          state.blocks.add(breakStatement);
          breakStatement.comment = "pseudo-goto";
          remove_branch(state, b);
        }
      }
      b = b.next;
    }
  }
  
  private static void find_do_blocks(State state, Declaration[] declList) {
    for(Declaration decl : declList) {
      int begin = decl.begin;
      if(!decl.forLoop && !decl.forLoopExplicit) {
        boolean needsDoEnd = true;
        for(Block block : state.blocks) {
          if(block.contains(decl.begin)) {
            if(block.scopeEnd() == decl.end) {
              block.useScope();
              needsDoEnd = false;
              break;
            } else if(block.scopeEnd() < decl.end) {
              begin = Math.min(begin, block.begin);
            }
          }
        }
        if(needsDoEnd) {
          // Without accounting for the order of declarations, we might
          // create another do..end block later that would eliminate the
          // need for this one. But order of decls should fix this.
          state.blocks.add(new DoEndBlock(state.function, begin, decl.end + 1));
        }
      }
    }
  }
  
  private static boolean is_conditional(Branch b) {
    return b.type == Branch.Type.comparison || b.type == Branch.Type.test;
  }
  
  private static boolean is_assignment(Branch b) {
    return b.type == Branch.Type.testset;
  }
  
  private static boolean is_assignment(Branch b, int r) {
    return b.type == Branch.Type.testset || b.type == Branch.Type.test && b.target == r;
  }
  
  private static boolean adjacent(State state, Branch branch0, Branch branch1) {
    if(branch0 == null || branch1 == null) {
      return false;
    } else {
      boolean adjacent = branch0.targetFirst <= branch1.line;
      if(adjacent) {
        adjacent = !has_statement(state, branch0.targetFirst, branch1.line - 1);
        adjacent = adjacent && !state.reverse_targets[branch1.line];
      }
      return adjacent;
    }
  }
  
  private static Branch combine_left(State state, Branch branch1) {
    if(is_conditional(branch1)) {
      return combine_conditional(state, branch1);
    } else if(is_assignment(branch1) || branch1.type == Branch.Type.finalset) {
      return combine_assignment(state, branch1);
    } else {
      return branch1;
    }
  }
  
  private static Branch combine_conditional(State state, Branch branch1) {
    Branch branch0 = branch1.previous;
    Branch branchn = branch1;
    while(branch0 != null && branchn == branch1) {
      branchn = combine_conditional_helper(state, branch0, branch1);
      if(branch0.targetSecond > branch1.targetFirst) break;
      branch0 = branch0.previous;
    }
    return branchn;
  }
  
  private static Branch combine_conditional_helper(State state, Branch branch0, Branch branch1) {
    if(adjacent(state, branch0, branch1) && is_conditional(branch0) && is_conditional(branch1)) {
      int branch0TargetSecond = branch0.targetSecond;
      if(is_jmp(state, branch1.targetFirst) && state.code.target(branch1.targetFirst) == branch0TargetSecond) {
        // Handle redirected target
        branch0TargetSecond = branch1.targetFirst;
      }
      if(branch0TargetSecond == branch1.targetFirst) {
        // Combination if not branch0 or branch1 then
        branch0 = combine_conditional(state, branch0);
        Condition c = new OrCondition(branch0.cond.inverse(), branch1.cond);
        Branch branchn = new Branch(branch0.line, Branch.Type.comparison, c, branch1.targetFirst, branch1.targetSecond);
        branchn.inverseValue = branch1.inverseValue;
        if(verbose) System.err.println("conditional or " + branchn.line);
        replace_branch(state, branch0, branch1, branchn);
        return combine_conditional(state, branchn);
      } else if(branch0TargetSecond == branch1.targetSecond) {
        // Combination if branch0 and branch1 then
        branch0 = combine_conditional(state, branch0);
        Condition c = new AndCondition(branch0.cond, branch1.cond);
        Branch branchn = new Branch(branch0.line, Branch.Type.comparison, c, branch1.targetFirst, branch1.targetSecond);
        branchn.inverseValue = branch1.inverseValue;
        if(verbose) System.err.println("conditional and " + branchn.line);
        replace_branch(state, branch0, branch1, branchn);
        return combine_conditional(state, branchn);
      }
    }
    return branch1;
  }
  
  private static Branch combine_assignment(State state, Branch branch1) {
    Branch branch0 = branch1.previous;
    Branch branchn = branch1;
    while(branch0 != null && branchn == branch1) {
      branchn = combine_assignment_helper(state, branch0, branch1);
      if(branch0.targetSecond > branch1.targetFirst) break;
      branch0 = branch0.previous;
    }
    return branchn;
  }
  
  private static Branch combine_assignment_helper(State state, Branch branch0, Branch branch1) {
    if(adjacent(state, branch0, branch1)) {
      int register = branch1.target;
      if(branch1.target == -1) {
        throw new IllegalStateException();
      }
      //System.err.println("blah " + branch1.line + " " + branch0.line);
      if(is_conditional(branch0) && is_assignment(branch1)) {
        //System.err.println("bridge cand " + branch1.line + " " + branch0.line);
        if(branch0.targetSecond == branch1.targetFirst) {
          boolean inverse = branch0.inverseValue;
          if(verbose) System.err.println("bridge " + (inverse ? "or" : "and") + " " + branch1.line + " " + branch0.line);
          branch0 = combine_conditional(state, branch0);
          if(inverse != branch0.inverseValue) throw new IllegalStateException();
          Condition c;
          if(!branch1.inverseValue) {
            //System.err.println("bridge or " + branch0.line + " " + branch0.inverseValue);
            c = new OrCondition(branch0.cond.inverse(), branch1.cond); 
          } else {
            //System.err.println("bridge and " + branch0.line + " " + branch0.inverseValue);
            c = new AndCondition(branch0.cond, branch1.cond);
          }
          Branch branchn = new Branch(branch0.line, branch1.type, c, branch1.targetFirst, branch1.targetSecond);
          branchn.inverseValue = branch1.inverseValue;
          branchn.target = register;
          replace_branch(state, branch0, branch1, branchn);
          return combine_assignment(state, branchn);
        } else if(branch0.targetSecond == branch1.targetSecond) {
          /*
          Condition c = new AndCondition(branch0.cond, branch1.cond);
          Branch branchn = new Branch(branch0.line, Branch.Type.comparison, c, branch1.targetFirst, branch1.targetSecond);
          replace_branch(state, branch0, branch1, branchn);
          return branchn;
          */
        }
      }
      
      if(is_assignment(branch0, register) && is_assignment(branch1) && branch0.inverseValue == branch1.inverseValue) {
        if(branch0.targetSecond == branch1.targetSecond) {
          Condition c;
          //System.err.println("preassign " + branch1.line + " " + branch0.line + " " + branch0.targetSecond);
          if(verbose) System.err.println("assign " + (branch0.inverseValue ? "or" : "and") + " " + branch1.line + " " + branch0.line);
          if(is_conditional(branch0)) {
            branch0 = combine_conditional(state, branch0);
            if(branch0.inverseValue) {
              branch0.cond = branch0.cond.inverse(); // inverse has been double handled; undo it
            }
          } else {
            boolean inverse = branch0.inverseValue;
            branch0 = combine_assignment(state, branch0);
            if(inverse != branch0.inverseValue) throw new IllegalStateException();
          }
          if(branch0.inverseValue) {
            //System.err.println("assign and " + branch1.line + " " + branch0.line);
            c = new OrCondition(branch0.cond, branch1.cond);
          } else {
            //System.err.println("assign or " + branch1.line + " " + branch0.line);
            c = new AndCondition(branch0.cond, branch1.cond);
          }
          Branch branchn = new Branch(branch0.line, branch1.type, c, branch1.targetFirst, branch1.targetSecond);
          branchn.inverseValue = branch1.inverseValue;
          branchn.target = register;
          replace_branch(state, branch0, branch1, branchn);
          return combine_assignment(state, branchn);
        }
      }
      if(is_assignment(branch0, register) && branch1.type == Branch.Type.finalset) {
        if(branch0.targetSecond == branch1.targetSecond) {
          Condition c;
          //System.err.println("final preassign " + branch1.line + " " + branch0.line);
          if(is_conditional(branch0)) {
            branch0 = combine_conditional(state, branch0);
            if(branch0.inverseValue) {
              branch0.cond = branch0.cond.inverse(); // inverse has been double handled; undo it
            }
          } else {
            boolean inverse = branch0.inverseValue;
            branch0 = combine_assignment(state, branch0);
            if(inverse != branch0.inverseValue) throw new IllegalStateException();
          }
          if(verbose) System.err.println("final assign " + (branch0.inverseValue ? "or" : "and") + " " + branch1.line + " " + branch0.line);
          
          if(branch0.inverseValue) {
            //System.err.println("final assign or " + branch1.line + " " + branch0.line);
            c = new OrCondition(branch0.cond, branch1.cond);
          } else {
            //System.err.println("final assign and " + branch1.line + " " + branch0.line);
            c = new AndCondition(branch0.cond, branch1.cond);
          }
          Branch branchn = new Branch(branch0.line, Branch.Type.finalset, c, branch1.targetFirst, branch1.targetSecond);
          branchn.target = register;
          replace_branch(state, branch0, branch1, branchn);
          return combine_assignment(state, branchn);
        }
      }
    }
    return branch1;
  }
  
  private static Branch[] branches(State state, Branch b) {
    if(b.type == Branch.Type.finalset) {
      return state.finalsetbranches;
    } else if(b.type == Branch.Type.testset) {
      return state.setbranches;
    } else {
      return state.branches;
    }
  }
  
  private static void replace_branch(State state, Branch branch0, Branch branch1, Branch branchn) {
    remove_branch(state, branch0);
    branches(state, branch1)[branch1.line] = null;
    branchn.previous = branch1.previous;
    if(branchn.previous == null) {
      state.begin_branch = branchn;
    } else {
      branchn.previous.next = branchn;
    }
    branchn.next = branch1.next;
    if(branchn.next == null) {
      state.end_branch = branchn;
    } else {
      branchn.next.previous = branchn;
    }
    branches(state, branchn)[branchn.line] = branchn;
  }
  
  private static void remove_branch(State state, Branch b) {
    branches(state, b)[b.line] = null;
    Branch prev = b.previous;
    Branch next = b.next;
    if(prev != null) {
      prev.next = next;
    } else {
      state.begin_branch = next;
    }
    if(next != null) {
      next.previous = prev;
    } else {
      state.end_branch = prev;
    }
  }
  
  private static void insert_branch(State state, Branch b) {
    branches(state, b)[b.line] = b;
  }
  
  private static void link_branches(State state) {
    Branch previous = null;
    for(int index = 0; index < state.branches.length; index++) {
      for(int array = 0; array < 3; array ++) {
        Branch[] branches;
        if(array == 0) {
          branches = state.finalsetbranches;
        } else if(array == 1) {
          branches = state.setbranches;
        } else {
          branches = state.branches;
        }
        Branch b = branches[index];
        if(b != null) {
          b.previous = previous;
          if(previous != null) {
            previous.next = b;
          } else {
            state.begin_branch = b;
          }
          previous = b;
        }
      }
    }
    state.end_branch = previous;
  }
  
  private static int get_target(State state, int line) {
    Code code = state.code;
    if(code.isUpvalueDeclaration(line)) {
      line--;
      while(code.op(line) != Op.CLOSURE) line--;
      int codepoint = code.codepoint(line);
      int target = Op.CLOSURE.target(codepoint, code.getExtractor());
      return target;
    } else {
      Op op = code.op(line);
      int codepoint = code.codepoint(line);
      int target = op.target(codepoint, code.getExtractor());
      if(target == -1) {
        // Special handling for table literals
        //  also TESTSET (since line will be JMP)
        switch(op) {
        case SETLIST:
        case SETLISTO:
        case SETLIST50:
        case SETLIST52:
        case SETTABLE:
          target = code.A(line);
          break;
        case EXTRABYTE:
          if(line >= 2 && code.op(line - 1) == Op.SETLIST) {
            target = code.A(line - 1);
          }
          break;
        case EXTRAARG:
          if(line >= 2 && code.op(line - 1) == Op.SETLIST52) {
            target = code.A(line - 1);
          }
          break;
        case JMP:
        case JMP52:
          if(line >= 2) {
            if(code.op(line - 1) == Op.TESTSET || code.op(line - 1) == Op.TEST50) {
              target = code.op(line - 1).target(code.codepoint(line - 1), code.getExtractor());
            }
          }
          break;
        default:
          break;
        }
      }
      return target;
    }
  }
  
  private static boolean is_jmp_raw(State state, int line) {
    Op op = state.code.op(line);
    return op == Op.JMP || op == Op.JMP52;
  }
  
  private static boolean is_jmp(State state, int line) {
    Code code = state.code;
    Op op = code.op(line);
    if(op == Op.JMP) {
      return true;
    } else if(op == Op.JMP52) {
      return !is_close(state, line);
    } else {
      return false;
    }
  }
  
  private static boolean is_close(State state, int line) {
    Code code = state.code;
    Op op = code.op(line);
    if(op == Op.CLOSE) {
      return true;
    } else if(op == Op.JMP52) {
      int target = code.target(line);
      if(target == line + 1) {
        return code.A(line) != 0;
      } else {
        if(line + 1 <= code.length && code.op(line + 1) == Op.JMP52) {
          return target == code.target(line + 1) && code.A(line) != 0;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }
  
  private static boolean has_statement(State state, int begin, int end) {
    for(int line = begin; line <= end; line++) {
      if(is_statement(state, line)) {
        return true;
      }
    }
    return state.d.hasStatement(begin, end);
  }
  
  private static boolean is_statement(State state, int line) {
    if(state.reverse_targets[line]) return true;
    Registers r = state.r;
    if(!r.getNewLocals(line).isEmpty()) return true;
    Code code = state.code;
    if(code.isUpvalueDeclaration(line)) return false;
    switch(code.op(line)) {
      case MOVE:
      case LOADK:
      case LOADKX:
      case LOADBOOL:
      case GETUPVAL:
      case GETTABUP:
      case GETGLOBAL:
      case GETTABLE:
      case NEWTABLE:
      case NEWTABLE50:
      case ADD:
      case SUB:
      case MUL:
      case DIV:
      case MOD:
      case POW:
      case IDIV:
      case BAND:
      case BOR:
      case BXOR:
      case SHL:
      case SHR:
      case UNM:
      case NOT:
      case LEN:
      case BNOT:
      case CONCAT:
      case CLOSURE:
      case TESTSET:
        return r.isLocal(code.A(line), line);
      case LOADNIL:
        for(int register = code.A(line); register <= code.B(line); register++) {
          if(r.isLocal(register, line)) {
            return true;
          }
        }
        return false;
      case LOADNIL52:
        for(int register = code.A(line); register <= code.A(line) + code.B(line); register++) {
          if(r.isLocal(register, line)) {
            return true;
          }
        }
        return false;
      case SETGLOBAL:
      case SETUPVAL:
      case SETTABUP:
      case TAILCALL:
      case RETURN:
      case FORLOOP:
      case FORPREP:
      case TFORCALL:
      case TFORLOOP:
      case TFORLOOP52:
      case TFORPREP:
      case CLOSE:
        return true;
      case TEST50:
        return code.A(line) != code.B(line) && r.isLocal(code.A(line), line);
      case SELF:
        return r.isLocal(code.A(line), line) || r.isLocal(code.A(line) + 1, line);
      case EQ:
      case LT:
      case LE:
      case TEST:
      case SETLIST:
      case SETLIST52:
      case SETLIST50:
      case SETLISTO:
      case EXTRAARG:
      case EXTRABYTE:
        return false;
      case JMP:
      case JMP52: // TODO: CLOSE?
        if(line == 1) {
          return true;
        } else {
          Op prev = line >= 2 ? code.op(line - 1) : null;
          Op next = line + 1 <= code.length ? code.op(line + 1) : null;
          if(prev == Op.EQ) return false;
          if(prev == Op.LT) return false;
          if(prev == Op.LE) return false;
          if(prev == Op.TEST) return false;
          if(prev == Op.TESTSET) return false;
          if(prev == Op.TEST50) return false;
          if(next == Op.LOADBOOL && code.C(line + 1) != 0) return false;
          return true;
        }
      case CALL: {
        int a = code.A(line);
        int c = code.C(line);
        if(c == 1) {
          return true;
        }
        if(c == 0) c = r.registers - a + 1;
        for(int register = a; register < a + c - 1; register++) {
          if(r.isLocal(register, line)) {
            return true;
          }
        }
        return false;
      }
      case VARARG: {
        int a = code.A(line);
        int b = code.B(line);
        if(b == 0) b = r.registers - a + 1;
        for(int register = a; register < a + b - 1; register++) {
          if(r.isLocal(register, line)) {
            return true;
          }
        }
        return false;
      }
      case SETTABLE:
        // special case -- this is actually ambiguous and must be resolved by the decompiler check
        return false;
    }
    throw new IllegalStateException("Illegal opcode: " + code.op(line));
  }
  
  // static only
  private ControlFlowHandler() {
  }
  
}
