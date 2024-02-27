package dao.IR;

import dao.IR.instructions.binary.Binary;
import dao.IR.instructions.global.Declare;
import dao.IR.instructions.label.Label;
import dao.IR.instructions.memory.*;
import dao.IR.instructions.terminator.Br;
import dao.IR.instructions.terminator.Call;
import dao.IR.instructions.terminator.Ret;
import dao.IR.nodes.BasicBlock;
import dao.IR.nodes.Function;
import dao.IR.nodes.IRModule;
import dao.IR.regs.Reg;
import dao.symbol.types.ArrayType;
import dao.symbol.types.Type;

import java.util.ArrayList;
import java.util.List;

public class IRBuilder {
    /*构造函数*/
    public IRBuilder(){}
    /*构造指令*/
    public void buildDeclare(IRModule irModule, Reg srcReg, Type returnType){
        /*srcReg就是存储函数名称的寄存器*/
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        Declare declare = new Declare(operand, returnType);
        irModule.addDeclare(declare);
    }
    public void buildAlloca(BasicBlock basicBlock, Reg result, Type allocaType){
        Alloca alloca = new Alloca(allocaType,result);
        basicBlock.addInstruction(alloca);
    }
    public void buildLoad(BasicBlock basicBlock, Reg result, Reg srcReg, Type loadType){
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        Load load = new Load(result,operand,loadType);
        basicBlock.addInstruction(load);
    }
    public void buildStore(BasicBlock basicBlock, Reg srcReg, Reg dstReg, Type storeType){
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        operand.add(dstReg);
        Store store = new Store(storeType,operand);
        basicBlock.addInstruction(store);
    }
    public void buildBinary(BasicBlock basicBlock,Reg result,String op,Reg op1,Reg op2,Type binaryType){
        /*一些指令有两个参数，op1和op2都非空，另一些指令只有一个参数，op2是空的*/
        List<Reg> operand = new ArrayList<>();
        operand.add(op1);
        operand.add(op2);
        Binary binary = new Binary(op,result,operand,binaryType);
        basicBlock.addInstruction(binary);
    }
    public void buildRet(BasicBlock basicBlock,Reg srcReg,Type returnType){
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        Ret ret = new Ret(operand,returnType);
        basicBlock.addInstruction(ret);
    }
    public void buildRet(BasicBlock basicBlock){
        Ret ret = new Ret();
        basicBlock.addInstruction(ret);
    }


    public void buildGEP(BasicBlock basicBlock,Reg result,Reg srcReg,Integer pos,Type gepType){
        /*getType是arrayType，srcReg是数组名称的寄存器*/
        /*先通过给定的pos转换为数组的维度坐标*/
        List<Integer> crd = ((ArrayType)gepType).getCoordinate(pos);
        /*先把Integer类型的坐标信息转换为GEP指令需要的Reg类型*/
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        operand.add(new Reg(0));/*GEP指令中第一条索引表示偏移量，设置为0*/
        for (Integer integer : crd) {
            Reg reg = new Reg(integer);
            operand.add(reg);
        }
        GEP gep = new GEP(result,operand,gepType);
        basicBlock.addInstruction(gep);
    }
    public void buildGEP(BasicBlock basicBlock,Reg result,Reg srcReg,List<Reg> crd,Type gepType){
        /*getType是arrayType*/
        /*直接根据给定的坐标完成GEP指令的构造*/
        /*先把Integer类型的坐标信息转换为GEP指令需要的Reg类型*/
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        operand.add(new Reg(0));/*GEP指令中第一条索引表示偏移量，设置为0*/
        operand.addAll(crd);
        GEP gep = new GEP(result,operand,gepType);
        basicBlock.addInstruction(gep);
    }
    public void buildGEP(BasicBlock basicBlock,Reg result,Reg srcReg,Reg offset,List<Reg> crd,Type gepType){
        /*getType是arrayType*/
        /*直接根据给定的坐标完成GEP指令的构造*/
        /*先把Integer类型的坐标信息转换为GEP指令需要的Reg类型*/
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        operand.add(offset);
        operand.addAll(crd);
        GEP gep = new GEP(result,operand,gepType);
        basicBlock.addInstruction(gep);
    }
    public void buildGEP(BasicBlock basicBlock,Reg offset,Reg result,Reg srcReg,Type gepType){
        /*对于数组指针，offset是有用的，先构造一条offset语句找到指针对应的数组再用正常的GEP指令执行*/
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        operand.add(offset);
        GEP gep = new GEP(result,operand,gepType);
        basicBlock.addInstruction(gep);
    }

    /*有返回值有参数列表的Call指令*/
    public void buildCall(BasicBlock basicBlock,Reg result,Reg funcNameReg,List<Reg> operand,Type callType){
        /*callType是functionType*/
        List<Reg> callOperand = new ArrayList<>();
        callOperand.add(funcNameReg);/*先把调用的函数名放进去*/
        callOperand.addAll(operand);/*再把已经给出的参数寄存器列表放进去*/
        Call call = new Call(result,callOperand,callType);
        basicBlock.addInstruction(call);
    }
    /*有返回值无参数列表的Call指令*/
    public void buildCall(BasicBlock basicBlock,Reg result,Reg funcNameReg,Type callType){
        /*callType是functionType*/
        List<Reg> callOperand = new ArrayList<>();
        callOperand.add(funcNameReg);/*先把调用的函数名放进去*/
        Call call = new Call(result,callOperand,callType);
        basicBlock.addInstruction(call);
    }
    /*无返回值无参数列表的Call指令*/
    public void buildCall(BasicBlock basicBlock,Reg funcNameReg,Type callType){
        List<Reg> operand = new ArrayList<>();
        operand.add(funcNameReg);
        Call call = new Call(operand,callType);
        basicBlock.addInstruction(call);
    }

    public void buildBr(BasicBlock basicBlock,Reg cmpReg,Reg label1,Reg label2){
        List<Reg> operand = new ArrayList<>();
        operand.add(cmpReg);
        operand.add(label1);
        operand.add(label2);
        Br br = new Br(operand);
        basicBlock.addInstruction(br);
    }
    public void buildBr(BasicBlock basicBlock,Reg label){
        List<Reg> operand = new ArrayList<>();
        operand.add(label);
        Br br = new Br(operand);
        basicBlock.addInstruction(br);
    }

    public BasicBlock buildCondBlock(Function function,BasicBlock basicBlock){
        /*给出当前的块以及当前的函数，创建一个条件表达式的块*/
        BasicBlock block = new BasicBlock();
        Reg label = function.labelBuilder.buildLabel();
        block.setLabel(label);
        block.setRegBuilder(basicBlock.regBuilder);
        block.setPrev(basicBlock);
        block.addInstruction(new Label(label));/*CondBlock都是有标签的，添加一条标签指令*/
        return block;
    }

    public void buildZext(BasicBlock basicBlock,Reg result,Reg srcReg,Type srcType,Type dstType){
        List<Reg> operand = new ArrayList<>();
        operand.add(srcReg);
        Zext zext = new Zext(result,operand,srcType,dstType);
        basicBlock.addInstruction(zext);
    }
}
