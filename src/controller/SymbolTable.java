package controller;

import dao.IR.regs.Reg;
import dao.symbol.TableEntry;
import dao.symbol.types.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable{
    private String name;/*当前符号表的名称，实际上没用但是debug的时候有用*/
    /*符号表主体部分*/
    private Map<String, TableEntry> table = new HashMap<>();

    /*构造函数*/
    public SymbolTable(){}
    public SymbolTable(String name) {
        this.name = name;
    }

    /*getter和setter*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, TableEntry> getTable() {
        return table;
    }

    public void setTable(Map<String, TableEntry> table) {
        this.table = table;
    }

    /*添加一条表项*/
    public void addSymbol(String name, Boolean isConst, Type type, Reg reg) {
        /*一般符号表里的isGlobal字段是false，并且局部变量全部需要调用RegBuilder生成寄存器，所以需要传入*/
        TableEntry entry = new TableEntry(name, isConst, type, reg);
        table.put(name, entry);
    }
    public void addSymbol(String name, Boolean isConst, Type type, Reg reg, Integer initVal) {
        /*全局变量必须有初始值*/
        TableEntry entry = new TableEntry(name, isConst, type, reg,initVal);
        table.put(name, entry);
    }
    public void addSymbol(String name, Boolean isConst, Type type, Reg reg, List<Integer> initVals) {
        /*全局数组变量必须有初始值*/
        TableEntry entry = new TableEntry(name, isConst, type, reg,initVals);
        table.put(name, entry);
    }

    /*只在当前表内查找名称*/
    public TableEntry findSymbol(String name){
        return table.get(name);
    }
}
