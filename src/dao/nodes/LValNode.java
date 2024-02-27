package dao.nodes;

import dao.Token;

import java.util.List;

public class LValNode {
    private Token ident;
    private List<Token> lBrackets;
    private List<ExpNode> exps;
    private List<Token> rBrackets;

    public LValNode(Token ident, List<Token> lBrackets, List<ExpNode> exps, List<Token> rBrackets) {
        this.ident = ident;
        this.lBrackets = lBrackets;
        this.exps = exps;
        this.rBrackets = rBrackets;
    }

    public Token getIdent() {
        return ident;
    }

    public List<Token> getlBrackets() {
        return lBrackets;
    }

    public List<ExpNode> getExps() {
        return exps;
    }

    public List<Token> getrBrackets() {
        return rBrackets;
    }

    @Override
    public String toString() {
        String res="";
        res+= ident.toString();
        if(!exps.isEmpty()){
            for(int i=0;i<exps.size();i++){
                res+=lBrackets.get(i).toString();
                res+= exps.get(i).toString();
                res+=rBrackets.get(i).toString();
            }
        }
        res+="<LVal>\n";
        return res;
    }
}
