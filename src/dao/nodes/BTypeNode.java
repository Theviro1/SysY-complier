package dao.nodes;

import dao.Token;

public class BTypeNode {
    private Token intToken;

    public BTypeNode(Token intToken) {
        this.intToken = intToken;
    }

    public Token getIntToken() {
        return intToken;
    }

    @Override
    public String toString() {
        return intToken.toString();
    }
}
