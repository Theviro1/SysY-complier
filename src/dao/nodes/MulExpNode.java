package dao.nodes;

import dao.Token;

public class MulExpNode {
    private UnaryExpNode unaryExp;
    private MulExpNode mulExp;
    private Token operator;

    public MulExpNode(UnaryExpNode unaryExp, MulExpNode mulExp, Token operator) {
        this.unaryExp = unaryExp;
        this.mulExp = mulExp;
        this.operator = operator;
    }

    public UnaryExpNode getUnaryExp() {
        return unaryExp;
    }

    public MulExpNode getMulExp() {
        return mulExp;
    }

    public Token getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return unaryExp.toString()+"<MulExp>\n"+(operator==null?"":operator+mulExp.toString());
    }
}
