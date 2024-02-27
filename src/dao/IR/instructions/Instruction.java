package dao.IR.instructions;

import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.Type;

import java.util.List;

public class Instruction {
    protected Reg result;/*指令结果寄存器*/
    protected List<Reg> operand;/*指令内的所有操作数寄存器*/
    protected InstructionType instructionType;/*指令的关键字（类型）*/
    protected Type operandType;/*操作对象的类型*/
    protected BasicBlock basicBlock;/*所属的BasicBlock*/
    protected Instruction prev, next;/*前驱和后继指令*/

    /*构造函数*/
    public Instruction(){}
    /*对于无左值结果的语句（例如load指令）*/
    public Instruction(List<Reg> operand){
        this.operand = operand;
    }
    public Instruction(List<Reg> operand, Type operandType){
        this.operand = operand;
        this.operandType = operandType;
    }
    /*对于有左值结果的语句*/
    public Instruction(Reg result, Type operandType){
        this.result = result;
        this.operandType = operandType;
    }
    public Instruction(Reg result, List<Reg> operand,Type operandType){
        this.result = result;
        this.operand = operand;
        this.operandType = operandType;
    }
    /*getter和setter*/
    public void setBasicBlock(BasicBlock basicBlock){
        this.basicBlock = basicBlock;
    }

    public Type getOperandType() {
        return operandType;
    }
}
