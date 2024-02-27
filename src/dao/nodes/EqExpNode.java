package dao.nodes;

import dao.Token;

public class EqExpNode {
    private RelExpNode relExp;
    private Token operator;
    private EqExpNode eqExp;

    public EqExpNode(RelExpNode relExp, Token operator, EqExpNode eqExp) {
        this.relExp = relExp;
        this.operator = operator;
        this.eqExp = eqExp;
    }

    public RelExpNode getRelExp() {
        return relExp;
    }

    public Token getOperator() {
        return operator;
    }

    public EqExpNode getEqExp() {
        return eqExp;
    }

    @Override
    public String toString() {
        /*左递归换成了右递归，为了避免重复输出<EqExp>，只在最后operator是null的最后一个表达式之后输出，否则每次递归都输出*/
        return relExp.toString() +"<EqExp>\n"+ (operator==null?"":operator+eqExp.toString());
    }
}
