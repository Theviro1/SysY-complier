package dao.nodes;

import dao.Token;

import java.util.List;

public class FuncFParamNode {
    BTypeNode bType;
    Token ident;
    List<Token> lBracket;
    List<Token> rBracket;
    List<ConstExpNode> constExps;

    public FuncFParamNode(BTypeNode bType, Token ident, List<Token> lBracket, List<Token> rBracket, List<ConstExpNode> constExps) {
        this.bType = bType;
        this.ident = ident;
        this.lBracket = lBracket;
        this.rBracket = rBracket;
        this.constExps = constExps;
    }

    public BTypeNode getbType() {
        return bType;
    }

    public Token getIdent() {
        return ident;
    }

    public List<Token> getlBracket() {
        return lBracket;
    }

    public List<Token> getrBracket() {
        return rBracket;
    }

    public List<ConstExpNode> getConstExps() {
        return constExps;
    }

    @Override
    public String toString() {
        String res="";
        res+= bType.toString();
        res+=ident.toString();
        if(!lBracket.isEmpty()){
            /*至少有一组*/
            res+=lBracket.get(0).toString();
            res+=rBracket.get(0).toString();
            for(int i=1;i<lBracket.size();i++){
                res+=lBracket.get(i).toString();
                res+=constExps.get(i-1).toString();
                res+=rBracket.get(i).toString();
            }
        }
        res+="<FuncFParam>\n";
        return res;
    }
}
