package dao.IR.instructions.global;

import dao.IR.instructions.Instruction;
import dao.IR.instructions.InstructionType;
import dao.IR.instructions.binary.Binary;
import dao.IR.nodes.BasicBlock;
import dao.IR.regs.Reg;
import dao.symbol.types.ArrayType;
import dao.symbol.types.Type;

import java.util.ArrayList;
import java.util.List;

/*语法：<result> = global | constant <ty> <value>*/
/*示例：%1 = constant i32 1*/
public class Global extends Instruction {
    private Boolean isConst;
    private Integer initVal;/*单一变量初始值*/
    private List<Integer> initVals;/*数组变量初始值*/
    private Boolean isEmpty;/*是否使用zeroInitializer字段完成赋值*/
    /*构造函数*/
    public Global(Reg result, Type operandType, Boolean isConst, Integer initVal){
        /*单一变量的构造函数*/
        super(result, operandType);
        this.initVal = initVal;
        this.instructionType = InstructionType.Global;
        this.isConst = isConst;
        this.initVals = null;
        this.isEmpty = false;
    }
    public Global(Reg result, Type operandType, Boolean isConst, List<Integer> initVals){
        /*数组变量的构造函数*/
        super(result, operandType);
        this.initVals = initVals;
        this.instructionType = InstructionType.Global;
        this.isConst = isConst;
        this.initVal = null;
        this.isEmpty = false;
    }
    public Global(Reg result, Type operandType, Boolean isConst, List<Integer> initVals, Boolean isEmpty){
        /*数组变量的构造函数*/
        super(result, operandType);
        this.initVals = initVals;
        this.instructionType = InstructionType.Global;
        this.isConst = isConst;
        this.initVal = null;
        this.isEmpty = isEmpty;
    }
    /*getter和setter*/


    /*高维全局数组表达式生成，需要使用递归函数*/
    private static String visitArr(Global thisGlobalArr){
        String thisString = "[";
        /*递归终点*/
        if(((ArrayType) thisGlobalArr.operandType).dimension() == 1){/*最终只剩下一维*/
            for(int i = 0; i < thisGlobalArr.initVals.size();i++){
                thisString += ((ArrayType)thisGlobalArr.operandType).getType();
                thisString += " ";
                thisString += thisGlobalArr.initVals.get(i);
                if(i == thisGlobalArr.initVals.size()-1) break;
                thisString += ", ";
            }
            thisString += "]";
            return thisString;
        }
        /*获取thisGlobalArr的基本信息：类型、初始值、当前维度大小*/
        ArrayType thisArrType = ((ArrayType) thisGlobalArr.operandType);
        List<Integer> thisArrDimensions = new ArrayList<>(thisArrType.getDimensions());/*传入数组的维度*/
        List<Integer> thisArrInitVals = new ArrayList<>(thisGlobalArr.initVals);/*传入数组的值*/
        int cnt = thisArrDimensions.get(0);/*当前维度大小*/
        /*构造nextGlobalArr的基本信息：类型、初始值*/
        List<Integer> nextArrDimensions = new ArrayList<>(thisArrDimensions.subList(1, thisArrDimensions.size()));
        ArrayType nextArrType = new ArrayType(thisArrType.getType(),nextArrDimensions);
        int nextCapacity = nextArrType.capacity();/*新数组的容量大小*/
        /*递归分析*/
        for(int i = 0; i < cnt; i++){/*当前分析的维度*/
            thisString += nextArrType.toString();
            List<Integer> nextInitVals = new ArrayList<>(thisArrInitVals.subList(i*nextCapacity,(i+1)*nextCapacity));
            System.out.println(thisArrInitVals);
            Global nextGlobalArr = new Global(null,nextArrType,null,nextInitVals);
            String nextString = visitArr(nextGlobalArr);
            thisString += nextString;
            if(i == cnt-1) break;
            thisString += ", ";
        }
        thisString += "]";
        return thisString;
    }
    @Override
    public String toString(){
        String res;
        res = result + " = ";
        if(this.isConst) res += "dso_local constant ";
        else res += "dso_local global ";
        res += operandType+" ";
        if(this.initVal == null){
            if(this.isEmpty) res += "zeroinitializer";/*没有初始值代表着整个数组都是zeroInitializer*/
            else res += visitArr(this);
        }
        else {
            res += initVal.toString();
        }
        return res;
    }
}
