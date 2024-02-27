package dao.IR.instructions.label;

import dao.IR.instructions.Instruction;
import dao.IR.regs.Reg;

import java.util.List;

public class Label extends Instruction {
    private Reg label;
    /*构造函数*/
    public Label(Reg label){
        super();
        this.label = label;
    }

    @Override
    public String toString(){
        return label.getOriginalName();
    }
}
