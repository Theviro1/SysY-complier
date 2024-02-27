package dao.nodes;

import dao.Token;

import java.util.List;

public class InitValNode {
    private ExpNode exp=null;
    private Token lBrace=null;
    private List<InitValNode> initVals=null;
    private List<Token> commas=null;
    private Token rBrace=null;

    public InitValNode(ExpNode exp) {
        this.exp = exp;
    }

    public InitValNode(Token lBrace, List<InitValNode> initVals, List<Token> commas, Token rBrace) {
        this.lBrace = lBrace;
        this.initVals = initVals;
        this.commas = commas;
        this.rBrace = rBrace;
    }

    public ExpNode getExp() {
        return exp;
    }

    public Token getlBrace() {
        return lBrace;
    }

    public List<InitValNode> getInitVals() {
        return initVals;
    }

    public List<Token> getCommas() {
        return commas;
    }

    public Token getrBrace() {
        return rBrace;
    }

    @Override
    public String toString() {
        String res="";
        /*initVals*/
        if(exp==null){
            res+= lBrace.toString();
            if(!initVals.isEmpty()){
                /*至少有一个*/
                res+=initVals.get(0).toString();
                for(int i=1;i<initVals.size();i++){
                    res+= commas.get(i-1).toString();
                    res+= initVals.get(i).toString();
                }
            }
            res+= rBrace.toString();
        }
        /*Exp*/
        else {
            res+= exp.toString();
        }
        res+="<InitVal>\n";
        return res;
    }
}
