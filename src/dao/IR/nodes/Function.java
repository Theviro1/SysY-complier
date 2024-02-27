package dao.IR.nodes;

import controller.SymbolTable;
import dao.IR.instructions.Instruction;
import dao.IR.regs.RegBuilder;
import dao.symbol.TableEntry;

import java.util.ArrayList;
import java.util.List;

public class Function {
    /*一个Function包括了一条自身生成的指令和内部一群基本块*/
    private BasicBlock block;/*函数基本块*/
    private Instruction functionInstruction;/*函数本身生成的定义指令*/
    private IRModule irModule;/*所属的父module*/
    public RegBuilder regBuilder;/*创建函数参数寄存器*/
    private List<Function> prev, next;/*所有前驱和后继节点的*/
    public SymbolTable symbolTable;/*函数的符号表，一般只会存储函数参数*/
    public RegBuilder labelBuilder;/*为函数内的基本块生成标签*/

    /*构造函数*/
    public Function(){
        this.labelBuilder = new RegBuilder();
        this.regBuilder = new RegBuilder();
        this.symbolTable = new SymbolTable();
    }
    public Function(List<Function> prev, List<Function> next){
        this.prev = prev;
        this.next = next;
        this.labelBuilder = new RegBuilder();
        this.regBuilder = new RegBuilder();
        this.symbolTable = new SymbolTable();
    }
    /*getter和setter*/
    public void setIrModule(IRModule irModule){
        this.irModule = irModule;
    }
    public RegBuilder getRegBuilder(){return this.regBuilder;}
    public void setFunctionInstruction(Instruction instruction){this.functionInstruction = instruction;}

    /*添加一个block*/
    public void setBlock(BasicBlock basicBlock){
        basicBlock.setFunction(this);
        this.block = basicBlock;
    }
    /*添加一个前驱或者后继block*/
    public void addPrev(Function prev){
        this.prev.add(prev);
    }
    public void addNext(Function next){
        this.next.add(next);
    }
    /*查找符号*/
    public TableEntry findSymbol(String name){
        TableEntry res = this.symbolTable.findSymbol(name);
        if(res == null){/*找不到符号表*/
            /*函数都找不到符号，那么一定是在全局符号表上找*/
            res = this.irModule.symbolTable.findSymbol(name);
        }
        return res;
    }

    @Override
    public String toString(){
        String res = "";
        res += this.functionInstruction + "{\n";
        res += this.block.toString();
        res += "}\n";
        return res;
    }
}
