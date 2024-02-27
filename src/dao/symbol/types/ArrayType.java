package dao.symbol.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayType implements Type{
    private Type type;/*数组类型*/
    private List<Integer> dimensions;/*每一维度的上界，dimension[i]表示第i+1维范围，*/

    public ArrayType(Type type, List<Integer> dimensions){
        this.type = type;
        this.dimensions = dimensions;
    }
    /*getter和setter*/

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    public List<Integer> getDimensions(){return dimensions;}


    /*获取整个数组的总容量*/
    public int capacity(){
        int cap = 1;
        for (Integer dimension : dimensions) {
            cap *= dimension;
        }
        return cap;
    }

    /*给定某个位置，获取对应的坐标*/
    public List<Integer> getCoordinate(Integer pos){
        List<Integer> crd = new ArrayList<>();
        int capacity = this.capacity();
        for(int i = 0;i < this.dimensions.size();i++){
            /*对于第i维，其高维容量等于当前总容量除以该维容量，以此来获取高纬的容量*/
            capacity = capacity / dimensions.get(i);
            /*计算crd第i维大小*/
            int result = pos / capacity;
            crd.add(result);
            pos %= capacity;
        }
        return crd;
    }
    /*给定某个坐标，获取对应的位置*/
    public Integer getPosition(List<Integer> crd){
        int pos = 0;
        int cap;
        for(int i=0;i<crd.size();i++){
            /*如果某一维出现了越界则返回null*/
            if(crd.get(i) > dimensions.get(i) - 1) return null;
            /*正常计算位置*/
            cap = 1;
            for(int j=i+1;j<crd.size();j++){
                cap *= dimensions.get(j);
            }
            pos += crd.get(i) * cap;
        }
        /*这里输出的pos就是最终的下标，比如输出1则表示是arr[1]，pos一定介于0和capacity-1之间（就像一个数组）*/
        return pos;
    }
    /*下降一维转化为指针*/
    public PointerType array2pointer(){
        List<Integer> subDimension = new ArrayList<>(this.dimensions.subList(1,dimensions.size()));
        return new PointerType(new ArrayType(this.type,subDimension));
    }
    /*下降多维数组*/
    public ArrayType array2array(int level){
        List<Integer> subDimension = new ArrayList<>(this.dimensions.subList(level,dimensions.size()));
        return new ArrayType(this.type,subDimension);
    }

    /*获取数组的维度*/
    public Integer dimension(){return dimensions.size();}

    @Override
    public boolean equals(Object object){
        if(this==object) return true;
        if(object==null||getClass()!=object.getClass()) return false;
        ArrayType arrayType = (ArrayType) object;
        return this.type==arrayType.type&&this.dimensions.equals(arrayType.getDimensions());
    }

    @Override
    public String toString(){
        String res="";
        /*如果dimensions是空说明是函数参数的指针类型，直接输出type即可*/
        if(dimensions.isEmpty()) return type.toString();
        for(int i = 0; i < this.dimension(); i++){
            res += "[";
            res += this.dimensions.get(i);
            res += " x ";
            if(i == dimensions.size()-1) res+=type;/*最后一维需要添加type信息*/
        }
        for(int i = 0; i < this.dimension(); i++) res += "]";
        return res;
    }

}
