package dao.symbol.types;

public class VarType implements Type{
    /*表示普通的类型，包括了i1、i8、i32和void四种类型，可以用于Function和Array内*/
    /*唯一字段是bit，包括了1、8、32三种类型*/
    private Integer bit;
    private VarType(Integer bit){
        this.bit = bit;
    }

    public static final VarType _void = new VarType(0);
    public static final VarType i1 = new VarType(1);
    public static final VarType i8 = new VarType(8);
    public static final VarType i32 = new VarType(32);

    @Override
    public String toString(){
        if(bit == 0) return "void";
        else return "i" + bit;
    }

}
