package dao.nodes;

public class ConstExpNode {
    private AddExpNode addExp;

    public ConstExpNode(AddExpNode addExp) {
        this.addExp = addExp;
    }

    public AddExpNode getAddExp() {
        return addExp;
    }

    @Override
    public String toString() {
        return addExp.toString() + "<ConstExp>\n";
    }
}
