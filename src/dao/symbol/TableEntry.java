package dao.symbol;

import dao.IR.regs.Reg;
import dao.symbol.types.Type;

import java.util.List;

public class TableEntry {
    /*所有字段使用public，直接访问而不再使用get*/
    public String name;/*变量名称*/
    public Boolean isConst;/*是否是常量*/
    public Type type;/*种类信息*/
    public Reg reg;/*对应的寄存器*/

    public Integer initVal;
    public List<Integer> initVals;
    /*构造函数*/
    /*一般符号表构造函数*/
    public TableEntry(String name, Boolean isConst, Type type, Reg reg){
        this.name = name;
        this.isConst = isConst;
        this.type = type;
        this.reg = reg;
        this.initVal = null;
        this.initVals = null;
    }
    public TableEntry(String name,Boolean isConst, Type type, Reg reg, Integer initVal){
        this.name = name;
        this.isConst = isConst;
        this.type = type;
        this.reg = reg;
        this.initVal = initVal;
    }
    public TableEntry(String name,Boolean isConst, Type type, Reg reg, List<Integer> initVals){
        this.name = name;
        this.isConst = isConst;
        this.type = type;
        this.reg = reg;
        this.initVals = initVals;
    }

}
