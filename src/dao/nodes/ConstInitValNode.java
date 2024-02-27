package dao.nodes;

import dao.Token;

import java.util.List;

public class ConstInitValNode {
    private ConstExpNode constExp;
    private Token lBrace;
    private List<ConstInitValNode> constInitVals;
    private List<Token> commas;
    private Token rBrace;

    public ConstInitValNode(ConstExpNode constExp, Token lBrace, List<ConstInitValNode> constInitVals, List<Token> commas, Token rBrace) {
        this.constExp = constExp;
        this.lBrace = lBrace;
        this.constInitVals = constInitVals;
        this.commas = commas;
        this.rBrace = rBrace;
    }

    public ConstExpNode getConstExp() {
        return constExp;
    }

    public Token getlBrace() {
        return lBrace;
    }

    public List<ConstInitValNode> getConstInitVals() {
        return constInitVals;
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
        /*如果不是ConstExp*/
        if(constExp==null){
            res+=lBrace.toString();
            /*判断是否有ConstInitVal*/
            if(!constInitVals.isEmpty()){
                /*至少有一个*/
                res+=constInitVals.get(0).toString();
                /*后面循环添加*/
                for(int i=1;i<constInitVals.size();i++){
                    res+=commas.get(i-1).toString();
                    res+=constInitVals.get(i).toString();
                }
            }
            res+=rBrace.toString();
        }
        /*如果是ConstExp*/
        else {
            res+= constExp.toString();
        }
        res+="<ConstInitVal>\n";
        return res;
    }
}
