package dao.symbol.types;

import java.util.List;

public class FunctionType implements Type{
    private Type returnType;/*返回值类型*/
    private List<Type> paramsType;/*记录参数列表的类型对象*/

    /*无参数函数的构造方法，没有参数列表*/
    public FunctionType(VarType returnType){
        this.returnType = returnType;
    }
    /*有参函数的构造方法，有参数列表*/
    public FunctionType(VarType returnType, List<Type> paramsType){
        this.returnType = returnType;
        this.paramsType = paramsType;
    }

    /*getter和setter*/

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public List<Type> getParamsType() {
        return paramsType;
    }

    public void setParamsType(List<Type> paramsType) {
        this.paramsType = paramsType;
    }


}
