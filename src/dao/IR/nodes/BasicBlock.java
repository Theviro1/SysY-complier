package dao.IR.nodes;

import controller.SymbolTable;
import dao.IR.instructions.Instruction;
import dao.IR.instructions.label.Label;
import dao.IR.regs.Reg;
import dao.IR.regs.RegBuilder;
import dao.symbol.TableEntry;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicBlock {
    /*基本字段信息*/
    public SymbolTable symbolTable;/*当前BasicBlock的符号表*/
    private BasicBlock prev;/*当前BasicBLock的前驱BasicBlock*/
    public RegBuilder regBuilder;/*当前BasicBlock独有的调用寄存器*/
    private Reg label;/*当前BasicBlock对应的标签*/
    private Function function;/*所属的父级Function*/
    private List<Instruction> instructions;/*所有内部指令的集合*/
    public List<Boolean> isJump;/*当前基本块内是否出现了跳转指令如break或continue，这直接影响到后续指令是否还有效*/

    /*构造函数*/
    public BasicBlock(){
        this.label = null;
        this.regBuilder = new RegBuilder();
        this.symbolTable = new SymbolTable();
        this.instructions = new ArrayList<>();
    }
    public BasicBlock(BasicBlock prev){
        /*只给定前驱基本块、后继基本块和所属的父级Function，其他参数可以在过程中生成*/
        this.label = null;
        this.prev = prev;
        this.regBuilder = new RegBuilder();/*创建一个全新的RegBuilder*/
        this.symbolTable = new SymbolTable();/*创建一个新的符号表*/
        this.instructions = new ArrayList<>();/*创建一个空的指令集合，等待添加指令*/
    }

    /*getter和setter*/
    public List<Instruction> getInstructions(){
        return this.instructions;
    }
    public void setRegBuilder(RegBuilder regBuilder){
        this.regBuilder = regBuilder;
    }
    public void setLabel(Reg label){this.label = label;}
    public void setFunction(Function function) {
        this.function = function;
    }
    public Reg getLabel(){return this.label;}

    /*添加一个前驱或后继BasicBlock*/
    public void setPrev(BasicBlock prev){
        this.prev = prev;
    }
    /*添加指令*/
    public void addInstruction(Instruction instruction){
        instruction.setBasicBlock(this);
        try {
            this.instructions.add(instruction);/*对于被锁定的列表不再添加元素*/
        }catch (Exception ignored){

        }
    }
    public void addInstructionList(List<Instruction> instructions){
        /*批量添加指令*/
        for(Instruction instruction:instructions){
            this.addInstruction(instruction);
        }
    }
    /*禁止修改指令列表*/
    public void lockInstructionList(){
        this.instructions = Collections.unmodifiableList(instructions);
    }
    /*查找符号表*/
    public TableEntry findSymbol(String name){
        TableEntry res = this.symbolTable.findSymbol(name);
        if(res == null){/*找不到符号表*/
            /*如果没有前驱basicBlock，说明这是第一个，查所属的函数的符号表*/
            if(this.prev == null){
                res = this.function.findSymbol(name);
            }else {
                /*由BasicBlock递归向上查找符号表*/
                res = this.prev.findSymbol(name);
            }
        }
        return res;
    }

    @Override
    public String toString(){
        String res = "";
        for(Instruction instruction:instructions){
            if(instruction instanceof Label){
                res += instruction.toString();
                res += ":\n";
            }
            else {
                res += "\t";
                res += instruction.toString();
                res += "\n";
            }
        }
        return res;
    }
}
