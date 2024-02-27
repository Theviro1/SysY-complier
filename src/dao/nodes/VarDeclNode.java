package dao.nodes;

import dao.Token;

import java.util.List;

public class VarDeclNode {
    private BTypeNode bType;
    private List<VarDefNode> varDefs;
    private List<Token> commas;
    private Token semicolon;

    public VarDeclNode(BTypeNode bType, List<VarDefNode> varDefs, List<Token> commas, Token semicolon) {
        this.bType = bType;
        this.varDefs = varDefs;
        this.commas = commas;
        this.semicolon = semicolon;
    }

    public BTypeNode getbType() {
        return bType;
    }

    public List<VarDefNode> getVarDefs() {
        return varDefs;
    }

    public List<Token> getCommas() {
        return commas;
    }

    public Token getSemicolon() {
        return semicolon;
    }

    @Override
    public String toString() {
        String res="";
        res+=bType.toString();
        res+=varDefs.get(0).toString();
        for(int i=1;i<varDefs.size();i++){
            res+=commas.get(i-1).toString();
            res+= varDefs.get(i).toString();
        }
        res+=semicolon.toString();
        res+="<VarDecl>\n";
        return res;
    }
}
