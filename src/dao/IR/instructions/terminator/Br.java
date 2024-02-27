package dao.IR.instructions.terminator;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;

import java.util.List;

/*语法：br i1 <cond>, label <if true>, label <if false> | br label <dest> 表示有条件跳转和无条件跳转*/
/*示例：br i1 %1, label %zone1, label %zone2
*      br label %zone*/
public class Br extends Instruction {
    /*构造函数*/
    public Br(List<Reg> operand){
        /*operand里有1项或者3项*/
        super(operand);
        this.instructionType = InstructionType.Br;
    }

    @Override
    public String toString(){
        String res;
        if(operand.size() == 1){/*只有一个参数一定是无条件跳转*/
            res = "br label " + operand.get(0);
        }else {
            res = "br i1 "+operand.get(0)+", label "+operand.get(1)+", label "+operand.get(2);
        }
        return res;
    }
}
