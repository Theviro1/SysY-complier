package dao.nodes;

import dao.Token;

import java.util.List;

public class VarDefNode {
    private Token ident;
    private List<Token> lBrackets;
    private List<ConstExpNode> constExps;
    private List<Token> rBrackets;
    private Token equal=null;
    private InitValNode initVal=null;

    public VarDefNode(Token ident, List<Token> lBrackets, List<ConstExpNode> constExps, List<Token> rBrackets) {
        this.ident = ident;
        this.lBrackets = lBrackets;
        this.constExps = constExps;
        this.rBrackets = rBrackets;
    }

    public VarDefNode(Token ident, List<Token> lBrackets, List<ConstExpNode> constExps, List<Token> rBrackets, Token equal, InitValNode initVal) {
        this.ident = ident;
        this.lBrackets = lBrackets;
        this.constExps = constExps;
        this.rBrackets = rBrackets;
        this.equal = equal;
        this.initVal = initVal;
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

    public InitValNode getInitVal() {
        return initVal;
    }

    @Override
    public String toString() {
        String res="";
        res+= ident.toString();
        if(!constExps.isEmpty()){
            for(int i=0;i<constExps.size();i++){
                res+=lBrackets.get(i).toString();
                res+=constExps.get(i).toString();
                res+=rBrackets.get(i).toString();
            }
        }
        if(equal!=null){
            res+=equal.toString();
            res+=initVal.toString();
        }
        res+="<VarDef>\n";
        return res;
    }
}
