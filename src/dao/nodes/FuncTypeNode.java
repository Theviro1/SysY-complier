package dao.nodes;

import dao.Token;

public class FuncTypeNode {
    private Token funcType;/*void int*/

    public FuncTypeNode(Token funcType) {
        this.funcType = funcType;
    }

    public Token getFuncType() {
        return funcType;
    }

    @Override
    public String toString() {
        return funcType.toString()+"<FuncType>\n";
    }
}
