package dao.nodes;

public class ExpNode {
    private AddExpNode addExp;

    public ExpNode(AddExpNode addExp) {
        this.addExp = addExp;
    }

    public AddExpNode getAddExp() {
        return addExp;
    }

    @Override
    public String toString() {
        return addExp.toString() + "<Exp>\n";
    }
}
