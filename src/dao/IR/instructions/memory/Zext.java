package dao.IR.instructions.memory;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.Type;

import java.util.List;

/*语法：<result> = zext <ty1> <value> to <ty2> 表示将类型从1转换到2*/
/*示例：%1 = zext i32 %2 to i8*/
public class Zext extends Instruction {
    private Type targetType;/*目标类型*/
    /*构造函数*/
    public Zext(Reg result, List<Reg> operand, Type operandType, Type targetType){
        super(result, operand, operandType);
        this.targetType = targetType;
        this.instructionType = InstructionType.Zext;
    }

    @Override
    public String toString(){
        return this.result+" = zext "+operandType+" "+operand.get(0)+" to "+targetType;
    }

}
