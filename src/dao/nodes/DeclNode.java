package dao.nodes;

public class DeclNode {
    private ConstDeclNode constDecl;
    private VarDeclNode varDecl;

    public DeclNode(ConstDeclNode constDecl, VarDeclNode varDecl) {
        this.constDecl = constDecl;
        this.varDecl = varDecl;
    }


    public ConstDeclNode getConstDecl() {
        return constDecl;
    }

    public VarDeclNode getVarDecl() {
        return varDecl;
    }

    @Override
    public String toString() {
        return constDecl==null? varDecl.toString(): constDecl.toString();
    }
}
