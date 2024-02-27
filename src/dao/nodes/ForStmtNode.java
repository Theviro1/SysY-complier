package dao.nodes;

import dao.Token;

public class ForStmtNode {
    private LValNode LVal;
    private Token equal;
    private ExpNode exp;

    public ForStmtNode(LValNode LVal, Token equal, ExpNode exp) {
        this.LVal = LVal;
        this.equal = equal;
        this.exp = exp;
    }

    public LValNode getLVal() {
        return LVal;
    }

    public Token getEqual() {
        return equal;
    }

    public ExpNode getExp() {
        return exp;
    }

    @Override
    public String toString() {
        String res="";
        res+=LVal.toString();
        res+=equal.toString();
        res+=exp.toString();
        res+="<ForStmt>\n";
        return res;
    }
}
