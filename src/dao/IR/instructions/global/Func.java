package dao.IR.instructions.global;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.regs.Reg;
import dao.symbol.types.FunctionType;
import dao.symbol.types.Type;

import java.util.List;

/*语法示例：define dso_local i32 @func(i32 %_1, i32 %_2) */
public class Func extends Instruction {
    /*输出函数定义语句*/

    /*构造函数*/
    public Func(List<Reg> operand, Type operandType){
        super(operand, operandType);
        this.instructionType = InstructionType.Func;
        this.operandType = operandType;
    }

    @Override
    public String toString(){
        String res;
        List<Type> paramsType = ((FunctionType)operandType).getParamsType();
        Type returnType = ((FunctionType)operandType).getReturnType();
        res = "define dso_local "+ returnType +" "+operand.get(0)+"(";
        /*operand第一个参数是函数名称，后面n个Reg对应n个参数一共n+1位，paramsType一共n位对应n个Reg的类型*/
        for(int i = 0;i < paramsType.size();i++){
            res += paramsType.get(i)+" ";
            res += operand.get(i+1);
            if(i == paramsType.size()-1) break;
            res += ", ";
        }
        res += ")";
        return res;
    }
}
