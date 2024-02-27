package dao.IR.instructions.memory;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.Type;

import java.util.List;

/*语法：store <ty> <value>, <ty>* <pointer> 表示把value存入到pointer指向的寄存器内*/
/*示例：store i32 %1, i32* @b */
public class Store extends Instruction {
    /*构造函数*/
    public Store(Type operandType, List<Reg> operands){
        super(operands, operandType);
        this.instructionType = InstructionType.Store;
    }

    @Override
    public String toString(){
        return "store "+operandType+" "+operand.get(0)+", "+operandType+"* "+operand.get(1);
    }

}
