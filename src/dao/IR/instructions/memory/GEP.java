package dao.IR.instructions.memory;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.ArrayType;
import dao.symbol.types.Type;

import java.util.List;

/*语法：<result> = getelementptr <ty>, <ty>* {, [inrange] <ty> <idx>}
*      <result> = getelementptr inbounds <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}
* 表示访问一个数组，按照每一个维度信息找到偏移量index，访问对应的指针*/
public class GEP extends Instruction {
    /*构造函数*/
    public GEP(Reg result, List<Reg> operand, Type operandType){
        /*operand内第一项是数组名寄存器，后面都是坐标*/
        super(result, operand, operandType);
        this.instructionType = InstructionType.GEP;
    }

    @Override
    public String toString(){
        String res=this.result+" = getelementptr "+operandType+", "+operandType+"* "+operand.get(0);
        for(int i=1;i<operand.size();i++){
            res+=", i32 " + operand.get(i);
        }
        return res;
    }
}
