package unluac.decompile;

import java.util.ArrayList;
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
import unluac.decompile.block.IfThenElseBlock;
import unluac.decompile.block.IfThenEndBlock;
import unluac.decompile.block.RepeatBlock;
import unluac.decompile.block.SetBlock;
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
    find_break_statements(state);
    find_if_blocks(state);
    find_set_blocks(state);
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
      if(code.isJMP(line)) {
        int target = code.target(line);
        if(target <= line) {
          reverse_targets[target] = true;
        }
      }
    }
  }
  
  private static void resolve_lines(State state) {
    int[] resolved = new int[state.code.length + 1];
    for(int line = 1; line <= state.code.length; line++) {
      int r = line;
      Branch b = state.branches[line];
      while(b != null && b.type == Branch.Type.jump) {
        r = b.targetSecond;
        b = state.branches[r];
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
    if(loadboolblock - 1 >= 1 && state.code.isJMP(loadboolblock - 1) && state.code.target(loadboolblock - 1) == loadboolblock + 2) {
      skip[loadboolblock - 1] = true;
      final_line = loadboolblock - 2;
    }
    boolean inverse = false;
    if(loadboolvalue == 1) {
      inverse = true;
      c = c.inverse();
    }
    boolean constant = state.code.isJMP(line);
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
    if(constant && final_line < begin && state.finalsetbranches[final_line + 1] == null) {
      c = new TestCondition(final_line + 1, state.code.A(target));
      b = new Branch(final_line + 1, Branch.Type.finalset, c, final_line, loadboolblock + 2);
      insert_branch(state, b);
    }
    if(final_line >= begin && state.finalsetbranches[final_line] == null) {
      c = new SetCondition(final_line, get_target(state, final_line));
      b = new Branch(final_line, Branch.Type.finalset, c, final_line, loadboolblock + 2);
      b.target = state.code.A(loadboolblock);
      insert_branch(state, b);
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
              if(code.C(line) != 0) c = c.inverse();
              int loadboolblock = find_loadboolblock(state, target);
              if(loadboolblock >= 1) {
                handle_loadboolblock(state, skip, loadboolblock, c, line, target);
              } else {
                Branch b = new Branch(line, Branch.Type.test, c, line + 2, target);
                b.target = code.A(line);
                if(code.C(line) != 0) b.inverseValue = true;
                insert_branch(state, b);
              }
            } else {
              Branch b = new Branch(line, Branch.Type.testset, c, line + 2, target);
              b.target = code.A(line);
              if(code.C(line) != 0) b.inverseValue = true;
              skip[line + 1] = true;
              insert_branch(state, b);
              int final_line = target - 1;
              if(state.finalsetbranches[final_line] == null) {
                int loadboolblock = find_loadboolblock(state, target - 2);
                if(loadboolblock == -1) {
                  if(line + 2 == target) {
                    c = new RegisterSetCondition(line, get_target(state, line));
                    final_line = final_line + 1;
                  } else {
                    c = new SetCondition(final_line, get_target(state, final_line));
                  }
                  b = new Branch(final_line, Branch.Type.finalset, c, target, target);
                  b.target = code.A(line);
                  insert_branch(state, b);
                }
              }
              break;
            }
            skip[line + 1] = true;
            break;
          }
          case TEST: {
            Condition c = new TestCondition(line, code.A(line));
            if(code.C(line) != 0) c = c.inverse();
            int target = code.target(line + 1);
            int loadboolblock = find_loadboolblock(state, target);
            if(loadboolblock >= 1) {
              handle_loadboolblock(state, skip, loadboolblock, c, line, target);
            } else {
              Branch b = new Branch(line, Branch.Type.test, c, line + 2, target);
              b.target = code.A(line);
              if(code.C(line) != 0) b.inverseValue = true;
              insert_branch(state, b);
            }
            skip[line + 1] = true;
            break;
          }
          case TESTSET: {
            Condition c = new TestCondition(line, code.B(line));
            int target = code.target(line + 1);
            Branch b = new Branch(line, Branch.Type.testset, c, line + 2, target);
            b.target = code.A(line);
            if(code.C(line) != 0) b.inverseValue = true;
            skip[line + 1] = true;
            insert_branch(state, b);
            int final_line = target - 1;
            if(state.finalsetbranches[final_line] == null) {
              int loadboolblock = find_loadboolblock(state, target - 2);
              if(loadboolblock == -1) {
                if(line + 2 == target) {
                  c = new RegisterSetCondition(line, get_target(state, line));
                  final_line = final_line + 1;
                } else {
                  c = new SetCondition(final_line, get_target(state, final_line));
                }
                b = new Branch(final_line, Branch.Type.finalset, c, target, target);
                b.target = code.A(line);
                insert_branch(state, b);
              }
            }
            break;
          }
          case JMP:
          case JMP52: {
            if(code.isJMP(line)) {
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
          if(close >= line + 1 && code.op(close) == Op.CLOSE && code.A(close) == A + 3) {
            forvarClose = true;
            close--;
          }
          if(close >= line + 1 && code.op(close) == Op.CLOSE && code.A(close) == A + 3 + C) {
            innerClose = true;
          }
          
          TForBlock block = new TForBlock(state.function, line + 1, target + 2, A, C, forvarClose, innerClose);
          block.handleVariableDeclarations(r);
          blocks.add(block);
        } else if(code.op(target) == forTarget && !loop[target]) {
          loop[target] = true;
          int A = code.A(target);
          r.setInternalLoopVariable(A, line, target);
          r.setInternalLoopVariable(A + 1, line, target);
          r.setInternalLoopVariable(A + 2, line, target);
          blocks.add(new ForBlock(state.function, line + 1, target + 1, A, false, false));
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
          if(close >= line + 1 && code.op(close) == Op.CLOSE && code.A(close) == A + 3) {
            forvarClose = true;
            close--;
          }
          if(close >= line + 1 && code.op(close) == Op.CLOSE && code.A(close) == A + 4) {
            innerClose = true;
          }
          
          ForBlock block = new ForBlock(state.function, line + 1, target + 1, A, forvarClose, innerClose);
          block.handleVariableDeclarations(r);
          blocks.add(block);
          break;
        }
        case TFORPREP: {
          int target = code.target(line);
          int A = code.A(target);
          int C = code.C(target);
          r.setInternalLoopVariable(A, line, target + 1);
          r.setInternalLoopVariable(A + 1, line, target + 1);
          for(int index = 0; index <= C; index++) {
            r.setExplicitLoopVariable(A + 2 + index, line, target + 1);
          }
          blocks.add(new TForBlock(state.function, line + 1, target + 2, A, C, false, false));
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
      if(j.type == Branch.Type.jump && j.targetFirst < j.line) {
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
          for(int l = loopback; l < b.line; l++) {
            
            if(is_statement(state, l)) {
              //System.err.println("not while " + l);
              b = null;
              break;
            }
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
          loop = new WhileBlock(state.function, b.cond, b.targetFirst, b.targetSecond);
          unredirect(state, loopback, end, j.line, loopback);
        } else {
          loop = new AlwaysLoop(state.function, loopback, end);
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
                for(int l = headb.targetSecond; l < b.line; l++) {
                  if(is_statement(state, l)) {
                    headb = null;
                    break;
                  }
                }
                if(headb != null) {
                  block = new WhileBlock(state.function, b.cond.inverse(), head + 1, b.targetFirst);
                  remove_branch(state, headb);
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
  
  private static void find_if_blocks(State state) {
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
        Branch tail = b.targetSecond >= 1 ? state.branches[b.targetSecond - 1] : null;
        if(tail != null && !is_conditional(tail)) {
          enclosing = enclosing_unprotected_block(state, tail.line);
          if(enclosing != null && !enclosing.contains(tail.targetSecond)) {
            if(tail.targetSecond == state.resolved[enclosing.getUnprotectedTarget()]) {
              tail.targetSecond = enclosing.getUnprotectedLine();
            }             
          }
          //System.err.println("else end " + b.targetFirst + " " + b.targetSecond + " " + tail.targetSecond + " enclosing " + (enclosing != null ? enclosing.begin : -1) + " " + + (enclosing != null ? enclosing.end : -1));
          state.blocks.add(new IfThenElseBlock(state.function, b.cond, b.targetFirst, b.targetSecond, tail.targetSecond));
          if(b.targetSecond != tail.targetSecond) {
            state.blocks.add(new ElseEndBlock(state.function, b.targetSecond, tail.targetSecond));
          } // else "empty else" case
          remove_branch(state, tail);
          unredirect(state, b.targetFirst, b.targetSecond, b.targetSecond - 1, tail.targetSecond);
        } else {
          //System.err.println("if end " + b.targetFirst + " " + b.targetSecond);
          
          Block breakable = enclosing_breakable_block(state, b.line);
          if(breakable != null && breakable.end == b.targetSecond) {
            // 5.2-style if-break
            Block block = new IfThenEndBlock(state.function, state.r, b.cond.inverse(), b.targetFirst - 1, b.targetFirst - 1);
            block.addStatement(new Break(state.function, b.targetFirst - 1, b.targetSecond));
            state.blocks.add(block);
          } else {
            state.blocks.add(new IfThenEndBlock(state.function, state.r, b.cond, b.targetFirst, b.targetSecond));
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
  
  private static Block enclosing_block(State state, Block inner) {
    Block enclosing = null;
    for(Block block : state.blocks) {
      if(block != inner && block.contains(inner) && block.breakable()) {
        if(enclosing == null || enclosing.contains(block)) {
          enclosing = block;
        }
      }
    }
    return enclosing;
  }
  
  private static void unredirect_break(State state, int line, Block enclosing) {
    Branch b = state.begin_branch;
    while(b != null) {
      Block breakable = enclosing_breakable_block(state, b.line);
      if(breakable != null && b.type == Branch.Type.jump && enclosing_block(state, breakable) == enclosing && b.targetFirst == enclosing.end) {
        b.targetFirst = line;
        b.targetSecond = line;
      }
      b = b.next;
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
        if(enclosing != null && b.targetSecond >= enclosing.end) {
          ifStack.add(b);
        }
      } else if(b.type == Branch.Type.jump) {
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
            Block block = new IfThenEndBlock(state.function, state.r, b.cond.inverse(), b.targetFirst - 1, b.targetFirst - 1);
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
  
  private static void find_do_blocks(State state, Declaration[] declList) {
    for(Declaration decl : declList) {
      if(!decl.forLoop && !decl.forLoopExplicit) {
        boolean needsDoEnd = true;
        for(Block block : state.blocks) {
          if(block.contains(decl.begin)) {
            if(block.scopeEnd() == decl.end) {
              block.useScope();
              needsDoEnd = false;
              break;
            }
          }
        }
        if(needsDoEnd) {
          // Without accounting for the order of declarations, we might
          // create another do..end block later that would eliminate the
          // need for this one. But order of decls should fix this.
          state.blocks.add(new DoEndBlock(state.function, decl.begin, decl.end + 1));
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
        for(int line = branch0.targetFirst; line < branch1.line; line++) {
          if(is_statement(state, line)) {
            if(verbose) System.out.println("Found statement at " + line + " between " + branch0.line + " and " + branch1.line);
            adjacent = false;
            break;
          }
        }
        adjacent = adjacent && !state.reverse_targets[branch1.line];
      }
      return adjacent;
    }
  }
  
  private static Branch combine_left(State state, Branch branch1) {
    if(is_conditional(branch1)) {
      return combine_conditional(state, branch1);
    } else {
      return combine_assignment(state, branch1);
    }
  }
  
  private static Branch combine_conditional(State state, Branch branch1) {
    Branch branch0 = branch1.previous;
    if(adjacent(state, branch0, branch1) && is_conditional(branch0) && is_conditional(branch1)) {
      int branch0TargetSecond = branch0.targetSecond;
      if(state.code.isJMP(branch1.targetFirst) && state.code.target(branch1.targetFirst) == branch0TargetSecond) {
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
    if(adjacent(state, branch0, branch1)) {
      int register = branch1.target;
      //System.err.println("blah " + branch1.line + " " + branch0.line);
      if(is_conditional(branch0) && is_assignment(branch1)) {
        //System.err.println("bridge cand " + branch1.line + " " + branch0.line);
        if(branch0.targetSecond == branch1.targetFirst) {
          boolean inverse = branch0.inverseValue;
          if(verbose) System.err.println("bridge " + (inverse ? "or" : "and") + " " + branch1.line + " " + branch0.line);
          branch0 = combine_conditional(state, branch0);
          if(inverse != branch0.inverseValue) throw new IllegalStateException();
          Condition c;
          if(inverse) {
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
        if(branch0.type == Branch.Type.test && branch0.inverseValue) {
          branch0.cond = branch0.cond.inverse(); // inverse has been double handled; undo it
        }
        if(branch0.targetSecond == branch1.targetSecond) {
          Condition c;
          //System.err.println("preassign " + branch1.line + " " + branch0.line + " " + branch0.targetSecond);
          boolean inverse = branch0.inverseValue;
          if(verbose) System.err.println("assign " + (inverse ? "or" : "and") + " " + branch1.line + " " + branch0.line);
          branch0 = combine_assignment(state, branch0);
          if(inverse != branch0.inverseValue) throw new IllegalStateException();
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
          if(branch0.type == Branch.Type.test && branch0.inverseValue) {
            branch0.cond = branch0.cond.inverse(); // inverse has been double handled; undo it
          }
          Condition c;
          //System.err.println("final preassign " + branch1.line + " " + branch0.line);
          boolean inverse = branch0.inverseValue;
          if(verbose) System.err.println("final assign " + (inverse ? "or" : "and") + " " + branch1.line + " " + branch0.line);
          branch0 = combine_assignment(state, branch0);
          if(inverse != branch0.inverseValue) throw new IllegalStateException();
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
    branches(state, branch0)[branch0.line] = null;
    branches(state, branch1)[branch1.line] = null;
    branchn.previous = branch0.previous;
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
    Op op = code.op(line);
    int codepoint = code.codepoint(line);
    return op.target(codepoint, code.getExtractor());
  }
  
  private static boolean is_statement(State state, int line) {
    if(state.reverse_targets[line]) return true;
    Registers r = state.r;
    int testRegister = -1;
    Code code = state.code;
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
        return r.isLocal(code.A(line), line) || code.A(line) == testRegister;
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
      case SETTABLE:
      case TAILCALL:
      case RETURN:
      case FORLOOP:
      case FORPREP:
      case TFORCALL:
      case TFORLOOP:
      case TFORPREP:
      case CLOSE:
        return true;
      case SELF:
        return r.isLocal(code.A(line), line) || r.isLocal(code.A(line) + 1, line);
      case EQ:
      case LT:
      case LE:
      case TEST:
      case TESTSET:
      case TEST50:
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
          Op prev = code.op(line - 1);
          if(prev == Op.EQ) return false;
          if(prev == Op.LT) return false;
          if(prev == Op.LE) return false;
          if(prev == Op.TEST) return false;
          if(prev == Op.TESTSET) return false;
          if(prev == Op.TEST50) return false;
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
        return (c == 2 && a == testRegister);
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
    }
    throw new IllegalStateException("Illegal opcode: " + code.op(line));
  }
  
  // static only
  private ControlFlowHandler() {
  }
  
}
