package dao.nodes;

public class BlockItemNode {
    private DeclNode decl=null;
    private StmtNode stmt=null;

    public BlockItemNode(DeclNode decl) {
        this.decl = decl;
    }

    public BlockItemNode(StmtNode stmt) {
        this.stmt = stmt;
    }

    public DeclNode getDecl() {
        return decl;
    }

    public StmtNode getStmt() {
        return stmt;
    }

    @Override
    public String toString() {
        return decl==null?stmt.toString():decl.toString();
    }
}
