package dao.nodes;

import dao.Token;

public class NumberNode {
    private Token intConst;

    public NumberNode(Token intConst) {
        this.intConst = intConst;
    }

    public Token getIntConst() {
        return intConst;
    }

    @Override
    public String toString() {
        return intConst.toString() + "<Number>\n";
    }

}
