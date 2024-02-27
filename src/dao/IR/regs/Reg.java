package dao.IR.regs;

public class Reg {
    /*寄存器类，用于记录所有的寄存器信息*/
    private String name;/*llvm代码中的寄存器名称，通常是%或@开头，如果是常数就直接存入*/
    private Boolean isGlobal;/*是否是全局寄存器*/


    /*构造方法*/
    public Reg(Integer number){
        /*常数临时寄存器*/
        this.name = number.toString();
    }
    public Reg(String originalName, Boolean isGlobal){
        /*符号临时寄存器*/
        if(isGlobal) name = "@" + originalName;
        else name = "%" + originalName;
        this.isGlobal = isGlobal;
    }

    /*getter和setter*/
    public String getName() {
        return name;
    }

    public Boolean getGlobal() {
        return isGlobal;
    }

    /*对于函数参数，获取其idx*/
    public String getOriginalName(){return name.substring(1);}

    @Override
    public String toString(){
        return this.name;
    }
}
