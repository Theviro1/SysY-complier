package dao.nodes;

import dao.Token;

import java.util.List;

public class FuncDefNode {
    private FuncTypeNode funcType;
    private Token ident;
    private Token lParent;
    private FuncFParamsNode funcFParams;
    private Token rParent;
    private BlockNode block;

    public FuncDefNode(FuncTypeNode funcType,Token ident,Token lParent,FuncFParamsNode funcFParams,Token rParent,BlockNode block) {
        this.funcType = funcType;
        this.ident = ident;
        this.lParent = lParent;
        this.funcFParams = funcFParams;
        this.rParent = rParent;
        this.block = block;
    }

    public FuncTypeNode getFuncType() {
        return funcType;
    }

    public Token getIdent() {
        return ident;
    }

    public Token getlParent() {
        return lParent;
    }

    public FuncFParamsNode getFuncFParams() {
        return funcFParams;
    }

    public Token getrParent() {
        return rParent;
    }

    public BlockNode getBlock() {
        return block;
    }

    @Override
    public String toString() {
        String res="";
        res+= funcType.toString();
        res+= ident.toString();
        res+= lParent.toString();
        if(funcFParams!=null){
            res+= funcFParams.toString();
        }
        res+=rParent.toString();
        res+= block.toString();
        res+="<FuncDef>\n";
        return res;
    }
}
