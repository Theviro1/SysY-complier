package dao.IR.instructions.memory;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.Type;

/*语法：<result> = alloca <type> 表示给result分配一块type大小的空间*/
/*举例：%1 = alloca i32*/
public class Alloca extends Instruction {
    /*构造函数*/
    public Alloca(Type operandType, Reg result){
        /*给定所属的基本块、所需要申请的类型、结果需要放入的寄存器即可*/
        super(result,null, operandType);/*alloca语句没有操作数operand，只有result*/
        this.instructionType = InstructionType.Alloca;
    }

    /*getter和setter*/



    @Override
    public String toString(){
        return this.result.toString() + " = alloca " + this.operandType;
    }
}
