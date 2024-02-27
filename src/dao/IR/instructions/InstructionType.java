package dao.IR.instructions;

public enum InstructionType {
    /*枚举所有的指令类型*/
    Global,Func,Decl,
    /*算术指令*/
    Add,Sub,Mul,Div,Mod,
    /*逻辑指令*/
    Lt,Le,Gt,Ge,Eq,Ne,And,Or,Not,
    /*终结指令*/
    Br,Call,Ret,
    /*内存指令*/
    Alloca,Load,Store,GEP,Zext,Phi
}
