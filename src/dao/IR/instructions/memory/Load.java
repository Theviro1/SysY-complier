package dao.IR.instructions.memory;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.PointerType;
import dao.symbol.types.Type;

import java.util.List;

/*语法：<result> = load <ty>, <ty>* <pointer> 表示把pointer指向的数据加载到result里*/
/*示例：%1 = load i32, i32* @a */
public class Load extends Instruction {
    /*构造函数*/
    public Load(Reg result, List<Reg> operand,Type operandType){
        /*提供所属的基本块、指针指向的数据类型、指令中的结果数和操作数即可*/
        super(result, operand,operandType);
        this.instructionType = InstructionType.Load;
    }

    @Override
    public String toString(){
        return this.result.toString()+" = load "+this.operandType+", "
                + this.operandType + "* " + this.operand.get(0).getName();
    }

}
