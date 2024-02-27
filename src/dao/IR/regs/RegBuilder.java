package dao.IR.regs;

public class RegBuilder {
    /*遇到局部变量的时候才需要分配一个新的寄存器，对于全局变量直接使用名字new Reg即可，其他局部都使用build方法*/
    private int count;/*自增的寄存器编号*/
    /*构造函数*/
    public RegBuilder(){
        count = 0;
    }
    /*构造一个新的临时寄存器*/
    public Reg build(){
        /*输入是否是全局变量，创建一个寄存器，形如%i*/
        Boolean isGlobal = false;/*只有局部需要中间寄存器，全局不需要，所以调用build一定是局部*/
        count++;/*count表示新的寄存器编号，也表示当前Builder里的总寄存器数量*/
        return new Reg(Integer.toString(count), isGlobal);
    }
    public Reg buildParam(){
        /*创建一个函数参数名称寄存器，形如%_i*/
        Boolean isGlobal = false;
        count++;
        String name = "_"+count;
        return new Reg(name,isGlobal);
    }
    public Reg buildLabel(){
        /*创建一个新的标签寄存器，形如%label_i*/
        Boolean isGlobal = false;/*标签一定是block的，block一定是局部的*/
        count++;
        String name = "label_"+count;
        return new Reg(name,isGlobal);
    }
}
