package dao.nodes;

import dao.Token;

public class UnaryExpNode {
    private PrimaryExpNode primaryExp=null;
    private Token ident=null;
    private Token lParent=null;
    private FuncRParamsNode funcRParams=null;
    private Token rParent=null;
    private UnaryOpNode unaryOp=null;
    private UnaryExpNode unaryExp=null;

    public UnaryExpNode(PrimaryExpNode primaryExp) {
        this.primaryExp = primaryExp;
    }

    public UnaryExpNode(Token ident, Token lParent, FuncRParamsNode funcRParams, Token rParent) {
        this.ident = ident;
        this.lParent = lParent;
        this.funcRParams = funcRParams;
        this.rParent = rParent;
    }

    public UnaryExpNode(UnaryOpNode unaryOp, UnaryExpNode unaryExp) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }

    public PrimaryExpNode getPrimaryExp() {
        return primaryExp;
    }

    public Token getIdent() {
        return ident;
    }

    public Token getlParent() {
        return lParent;
    }

    public FuncRParamsNode getFuncRParams() {
        return funcRParams;
    }

    public Token getrParent() {
        return rParent;
    }

    public UnaryOpNode getUnaryOp() {
        return unaryOp;
    }

    public UnaryExpNode getUnaryExp() {
        return unaryExp;
    }

    @Override
    public String toString() {
        String res="";
        /*判断PrimaryExp*/
        if(primaryExp!=null){
            res+=primaryExp.toString();
        }
        /*判断Ident FuncRParams*/
        else if (ident!=null) {
            res+=ident.toString();
            res+=lParent.toString();
            if(funcRParams!=null){
                res+=funcRParams.toString();
            }
            res+=rParent.toString();
        }
        /*判断UnaryOp*/
        else {
            res+=unaryOp.toString();
            res+=unaryExp.toString();
        }
        res+="<UnaryExp>\n";
        return res;
    }
}
