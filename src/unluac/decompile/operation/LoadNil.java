package unluac.decompile.operation;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Registers;
import unluac.decompile.block.Block;
import unluac.decompile.expression.ConstantExpression;
import unluac.decompile.expression.Expression;
import unluac.decompile.statement.Assignment;
import unluac.decompile.statement.Statement;

public class LoadNil extends Operation {

  public final int registerFirst;
  public final int registerLast;
  
  public LoadNil(int line, int registerFirst, int registerLast) {
    super(line);
    this.registerFirst = registerFirst;
    this.registerLast = registerLast;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    List<Statement> assignments = new ArrayList<Statement>(registerLast - registerFirst + 1);
    Expression nil = ConstantExpression.createNil(line);
    Assignment declare = null;
    int scopeEnd = -1;
    for(int register = registerFirst; register <= registerLast; register++) {
      if(r.isAssignable(register, line)) {
        scopeEnd = r.getDeclaration(register, line).end;
      }
    }
    for(int register = registerFirst; register <= registerLast; register++) {
      r.setValue(register, line, nil);
      if(r.isAssignable(register, line) && r.getDeclaration(register, line).end == scopeEnd) {
        if((r.getDeclaration(register, line).begin == line)) {
          if(declare == null) {
            declare = new Assignment();
            assignments.add(declare);
          }
          declare.addLast(r.getTarget(register, line), nil, line);
        } else {
          assignments.add(new Assignment(r.getTarget(register, line), nil, line));
        }
      }
    }
    return assignments;
   }
  
}
