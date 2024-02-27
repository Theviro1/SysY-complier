package dao.nodes;

import dao.Token;

import java.util.List;

public class ConstDefNode {
    private Token ident;
    private List<Token> lBrackets;
    private List<ConstExpNode> constExps;
    private List<Token> rBrackets;
    private Token equal;
    private ConstInitValNode constInitVal;

    public ConstDefNode(Token ident, List<Token> lBrackets, List<ConstExpNode> constExps, List<Token> rBrackets, Token equal, ConstInitValNode constInitVal) {
        this.ident = ident;
        this.lBrackets = lBrackets;
        this.constExps = constExps;
        this.rBrackets = rBrackets;
        this.equal = equal;
        this.constInitVal = constInitVal;
    }

    public Token getIdent() {
        return ident;
    }

    public List<Token> getlBrackets() {
        return lBrackets;
    }

    public List<ConstExpNode> getConstExps() {
        return constExps;
    }

    public List<Token> getrBrackets() {
        return rBrackets;
    }

    public Token getEqual() {
        return equal;
    }

    public ConstInitValNode getConstInitVal() {
        return constInitVal;
    }

    @Override
    public String toString() {
        String res="";
        res+= ident.toString();
        for(int i=0;i<constExps.size();i++){
            res+= lBrackets.get(i).toString();
            res+= constExps.get(i).toString();
            res+= rBrackets.get(i).toString();
        }
        res+=equal.toString();
        res+= constInitVal.toString();
        res+= "<ConstDef>\n";
        return res;
    }
}
