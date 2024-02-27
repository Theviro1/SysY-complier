package dao.nodes;

import dao.Token;

import java.util.List;

public class ConstDeclNode {
    private Token constToken;
    private BTypeNode bType;
    private List<Token> commas;
    private List<ConstDefNode> constDefs;
    private Token semicolon;

    public ConstDeclNode(Token constToken, BTypeNode bType, List<Token> commas, List<ConstDefNode> constDefs, Token semicolon) {
        this.constToken = constToken;
        this.bType = bType;
        this.commas = commas;
        this.constDefs = constDefs;
        this.semicolon = semicolon;
    }

    public Token getConstToken() {
        return constToken;
    }

    public BTypeNode getbType() {
        return bType;
    }

    public List<Token> getCommas() {
        return commas;
    }

    public List<ConstDefNode> getConstDefs() {
        return constDefs;
    }

    public Token getSemicolon() {
        return semicolon;
    }

    @Override
    public String toString() {
        String res="";
        res+=constToken.toString();
        res+=bType.toString();
        res+=constDefs.get(0).toString();
        for(int i=1;i<constDefs.size();i++){
            res+=commas.get(i-1).toString();
            res+=constDefs.get(i);
        }
        res+=semicolon.toString();
        res+="<ConstDecl>\n";
        return res;
    }
}
