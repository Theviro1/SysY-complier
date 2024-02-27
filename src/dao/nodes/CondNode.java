package dao.nodes;

public class CondNode {
    private LOrExpNode LOrExp;

    public CondNode(LOrExpNode LOrExp) {
        this.LOrExp = LOrExp;
    }

    public LOrExpNode getLOrExp() {
        return LOrExp;
    }

    @Override
    public String toString() {
        return LOrExp.toString() + "<Cond>\n";
    }
}
