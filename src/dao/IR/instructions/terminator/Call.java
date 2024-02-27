package dao.IR.instructions.terminator;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.FunctionType;
import dao.symbol.types.Type;
import dao.symbol.types.VarType;

import java.util.List;

/*语法：<result> = call [ret attrs] <ty> <fnptrval>(<function args>)*/
/*示例：%1 = call i32 @add(i32 %2, i32 %3)
*      call void @print() */
public class Call extends Instruction {
    /*构造函数*/

    /*注意operandType直接是Type即可*/
    /*无返回值函数*/
    public Call(List<Reg> operand, Type operandType){
        /*operand里有至少1项，其余的n项都是参数，operandType.getParamsType里也有n项*/
        super(operand, operandType);
        this.instructionType = InstructionType.Call;
    }
    /*有返回值函数*/
    public Call(Reg result, List<Reg> operand, Type operandType){
        /*operand里有至少1项，其余的n项都是参数，operandType.getParamsType里也有n项*/
        super(result, operand, operandType);
        this.instructionType = InstructionType.Call;
    }

    @Override
    public String toString(){
        Type returnType = ((FunctionType) operandType).getReturnType();
        List<Type> paramsType = ((FunctionType) operandType).getParamsType();
        String res;
        if(returnType.equals(VarType._void)){
            res = "call ";
        }else {
            res = result + " = call ";
        }
        res += returnType + " " + operand.get(0) + "(";/*函数名称*/
        for(int i=0;i<paramsType.size();i++){
            /*operand第一个参数是函数名称，后面n个Reg对应n个参数一共n+1位，paramsType一共n位对应n个Reg的类型*/
            res += paramsType.get(i) + " ";
            res += operand.get(i+1).toString();
            if(i == paramsType.size()-1) break;
            res += ", ";
        }
        res += ")";
        return res;
    }
}
