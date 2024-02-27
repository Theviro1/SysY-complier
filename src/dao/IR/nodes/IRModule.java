package dao.IR.nodes;

import controller.SymbolTable;
import dao.IR.instructions.Instruction;
import dao.IR.instructions.global.Declare;
import dao.IR.instructions.global.Global;
import dao.symbol.TableEntry;

import java.util.ArrayList;
import java.util.List;

public class IRModule {
    private List<Declare> declares;/*所有的库函数声明*/
    private List<Global> globals;/*所有全局变量的列表，包括Global类型和Arr类型*/
    private List<Function> functions;/*所有函数的列表*/
    public SymbolTable symbolTable;/*全局符号表*/

    private static final IRModule irModule = new IRModule();
    /*构造函数*/
    private IRModule(){
        this.symbolTable = new SymbolTable();
        this.globals = new ArrayList<>();
        this.declares = new ArrayList<>();
        this.functions = new ArrayList<>();
    }
    /*获取单例*/
    public static IRModule getIRModule(){
        return irModule;
    }

    /*添加函数*/
    public void addDeclare(Declare declare){
        this.declares.add(declare);
    }
    public void addFunction(Function function){
        function.setIrModule(this);
        functions.add(function);
    }
    public void addGlobal(Global global){
        globals.add(global);
    }
    /*查找符号*/
    public TableEntry findSymbol(String name){
        return this.symbolTable.findSymbol(name);
    }

    @Override
    public String toString(){
        String res="";
        for(Declare declare:declares){
            res += declare.toString();
            res += "\n";
        }
        for(Global global:globals){
            res += global.toString();
            res += "\n";
        }
        for(Function function:functions){
            res += function.toString();
        }
        return res;
    }
}
