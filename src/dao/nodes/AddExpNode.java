package dao.nodes;

import dao.Token;

import java.util.List;

public class AddExpNode {
    private MulExpNode mulExp;
    private AddExpNode addExp;
    private Token operator;

    public AddExpNode(MulExpNode mulExp, AddExpNode addExp, Token operator) {
        this.mulExp = mulExp;
        this.addExp = addExp;
        this.operator = operator;
    }

    public MulExpNode getMulExp() {
        return mulExp;
    }

    public AddExpNode getAddExp() {
        return addExp;
    }

    public Token getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return mulExp.toString() + "<AddExp>\n" + (operator==null?"":operator + addExp.toString());
    }
}
