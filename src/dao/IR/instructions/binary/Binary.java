package dao.IR.instructions.binary;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.Type;

import java.util.List;

/*所有算数计算指令比如add、sub、mul、div、mod都封装成这一个类*/
/*语法： <result> = <op> <ty> <op1>, <op2>
* 其中op包括add sub mul div lt le gt ge eq ne and or*/
/*示例：%1 = add i32 %2, %3
*      %1 = icmp slt %2, %3*/
public class Binary extends Instruction {
    private String operator;
    /*构造函数*/
    public Binary(String operator,Reg result, List<Reg> operand, Type operandType){
        /*operand里只有0~2项*/
        super(result, operand, operandType);
        this.operator = operator;
        switch (operator) {
            case "+" -> this.instructionType = InstructionType.Add;
            case "-" -> this.instructionType = InstructionType.Sub;
            case "*" -> this.instructionType = InstructionType.Mul;
            case "/" -> this.instructionType = InstructionType.Div;
            case "%" -> this.instructionType = InstructionType.Mod;
            case "<" -> this.instructionType = InstructionType.Lt;
            case "<=" -> this.instructionType = InstructionType.Le;
            case ">=" -> this.instructionType = InstructionType.Ge;
            case ">" -> this.instructionType = InstructionType.Gt;
            case "==" -> this.instructionType = InstructionType.Eq;
            case "!=" -> this.instructionType = InstructionType.Ne;
            case "&&" -> this.instructionType = InstructionType.And;
            case "||" -> this.instructionType = InstructionType.Or;
            case "!" -> this.instructionType = InstructionType.Not;
            default -> {
            }
        }
    }

    @Override
    public String toString(){
        String res;
        switch (operator) {
            case "+" -> res = result + " = " + "add " + operandType + " " + operand.get(0)+", "+operand.get(1);
            case "-" -> res = result + " = " + "sub " + operandType + " " + operand.get(0)+", "+operand.get(1);
            case "*" -> res = result + " = " + "mul " + operandType + " " + operand.get(0)+", "+operand.get(1);
            case "/" -> res = result + " = " + "sdiv " + operandType + " " + operand.get(0)+", "+operand.get(1);
            case "%" -> res = result + " = " + "srem " + operandType + " " + operand.get(0)+", "+operand.get(1);
            case "<" -> res = result + " = icmp slt " + operandType + " " + operand.get(0)+ ", "+operand.get(1);
            case "<=" -> res = result + " = icmp sle "+ operandType + " " + operand.get(0)+ ", "+operand.get(1);
            case ">=" -> res = result + " = icmp sge "+ operandType + " " + operand.get(0)+ ", "+operand.get(1);
            case ">" -> res = result + " = icmp sgt "+ operandType + " " + operand.get(0)+ ", "+operand.get(1);
            case "==" -> res = result + " = icmp eq "+ operandType + " " + operand.get(0)+ ", "+operand.get(1);
            case "!=" -> res = result + " = icmp ne "+ operandType + " " + operand.get(0)+ ", "+operand.get(1);
            case "!" -> res = result + " = " + "not " + operandType + " " + operand.get(0);
            default -> res = "";
        }
        return res;
    }
}
