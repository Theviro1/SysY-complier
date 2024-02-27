package dao.symbol.types;

public class PointerType implements Type{
    private Type target;/*记录了指向对象的Type信息*/

    public PointerType(Type target){
        this.target = target;
    }

    public Type getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return target.toString() + "*";
    }
}
