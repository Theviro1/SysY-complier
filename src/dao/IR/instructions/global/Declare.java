package dao.IR.instructions.global;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.regs.Reg;
import dao.symbol.types.FunctionType;
import dao.symbol.types.Type;
import dao.symbol.types.VarType;

import java.util.List;

public class Declare extends Instruction {
    /*构造函数*/
    public Declare(List<Reg> operand,Type operandType){
        /*operand内只有一个寄存器，由于Declare一定是函数类型，Type同Func指令一样是FunctionType*/
        super(operand, operandType);
        this.instructionType = InstructionType.Decl;
    }
    @Override
    public String toString(){
        String res = "declare ";
        Type returnType = ((FunctionType)operandType).getReturnType();
        List<Type> paramsType = ((FunctionType)operandType).getParamsType();
        res += returnType + " " + operand.get(0)+"(";
        if(!paramsType.isEmpty()){
            for(int i = 0;i < paramsType.size();i++){
                res += paramsType.get(i);
                if(i == paramsType.size()-1) break;
                res += ", ";
            }
        }
        res += ")";
        return res;
    }
}
