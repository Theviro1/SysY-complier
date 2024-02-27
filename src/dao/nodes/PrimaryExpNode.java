package dao.nodes;

import dao.Token;

public class PrimaryExpNode {
    private Token lParent=null;
    private ExpNode exp=null;
    private Token rParent=null;
    private LValNode LVal=null;
    private NumberNode number=null;

    public PrimaryExpNode(Token lParent, ExpNode exp, Token rParent) {
        this.lParent = lParent;
        this.exp = exp;
        this.rParent = rParent;
    }

    public PrimaryExpNode(LValNode LVal) {
        this.LVal = LVal;
    }

    public PrimaryExpNode(NumberNode number) {
        this.number = number;
    }

    public Token getlParent() {
        return lParent;
    }

    public ExpNode getExp() {
        return exp;
    }

    public Token getrParent() {
        return rParent;
    }

    public LValNode getLVal() {
        return LVal;
    }

    public NumberNode getNumber() {
        return number;
    }

    @Override
    public String toString() {
        String res="";
        /*判断是否是number*/
        if(number!=null){
            res+= number.toString();
        }
        /*判断是否是LVal*/
        else if (LVal!=null) {
            res+= LVal.toString();
        }
        /*剩下的是exp*/
        else {
            res+= lParent.toString();
            res+= exp.toString();
            res+= rParent.toString();
        }
        res+="<PrimaryExp>\n";
        return res;
    }
}
