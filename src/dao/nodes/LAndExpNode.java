package dao.nodes;

import dao.Token;

public class LAndExpNode {
    private EqExpNode eqExp;
    private Token operator;
    private LAndExpNode LAndExp;

    public LAndExpNode(EqExpNode eqExp, Token operator, LAndExpNode LAndExp) {
        this.eqExp = eqExp;
        this.operator = operator;
        this.LAndExp = LAndExp;
    }

    public EqExpNode getEqExp() {
        return eqExp;
    }

    public Token getOperator() {
        return operator;
    }

    public LAndExpNode getLAndExp() {
        return LAndExp;
    }

    @Override
    public String toString() {
        return eqExp.toString()+"<LAndExp>\n"+(operator==null?"":operator+LAndExp.toString());
    }
}
