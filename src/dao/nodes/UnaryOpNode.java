package dao.nodes;

import dao.Token;

public class UnaryOpNode {
    private Token operator;

    public UnaryOpNode(Token operator) {
        this.operator = operator;
    }

    public Token getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return operator.toString() + "<UnaryOp>\n";
    }
}
