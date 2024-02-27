package dao.nodes;

import dao.Token;

public class LOrExpNode {
    private LAndExpNode LAndExp;
    private Token operator;
    private LOrExpNode LOrExp;

    public LOrExpNode(LAndExpNode LAndExp, Token operator, LOrExpNode LOrExp) {
        this.LAndExp = LAndExp;
        this.operator = operator;
        this.LOrExp = LOrExp;
    }

    public LAndExpNode getLAndExp() {
        return LAndExp;
    }

    public Token getOperator() {
        return operator;
    }

    public LOrExpNode getLOrExp() {
        return LOrExp;
    }

    @Override
    public String toString() {
        return LAndExp.toString()+"<LOrExp>\n"+(operator==null?"":operator+LOrExp.toString());
    }
}
