package dao.IR.instructions.terminator;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.Type;
import dao.symbol.types.VarType;

import java.util.ArrayList;
import java.util.List;

/*语法：ret <type> <value> | void*/
/*示例：ret i32 %1
*      ret void */
public class Ret extends Instruction {
    /*构造函数*/
    /*有返回值*/
    public Ret(List<Reg> operand, Type operandType){
        /*operand里有1项，就是返回值所在的寄存器，operandType就是操作数的类型*/
        super(operand, operandType);
        this.instructionType = InstructionType.Ret;
    }
    /*无返回值*/
    public Ret(){
        super();
        this.operand = null;
        this.instructionType = InstructionType.Ret;
    }

    @Override
    public String toString(){
        String res;
        if(operand==null){/*有操作数*/
            res = "ret void";
        } else {
            res = "ret "+operandType+" "+operand.get(0);
        }
        return res;
    }
}
