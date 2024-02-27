package dao.nodes;

import dao.Token;

public class RelExpNode {
    private AddExpNode addExp;
    private Token operator;
    private RelExpNode relExpNode;

    public RelExpNode(AddExpNode addExp, Token operator, RelExpNode relExpNode) {
        this.addExp = addExp;
        this.operator = operator;
        this.relExpNode = relExpNode;
    }

    public AddExpNode getAddExp() {
        return addExp;
    }

    public Token getOperator() {
        return operator;
    }

    public RelExpNode getRelExp() {
        return relExpNode;
    }

    @Override
    public String toString() {
        return addExp.toString()+"<RelExp>\n"+(operator==null?"":operator+relExpNode.toString());
    }
}
