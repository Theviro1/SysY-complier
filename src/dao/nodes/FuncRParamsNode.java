package dao.nodes;

import dao.Token;

import java.util.List;

public class FuncRParamsNode {
    private List<ExpNode> exps;
    private List<Token> commas;

    public FuncRParamsNode(List<ExpNode> exps, List<Token> commas) {
        this.exps = exps;
        this.commas = commas;
    }

    public List<ExpNode> getExps() {
        return exps;
    }

    public List<Token> getCommas() {
        return commas;
    }

    @Override
    public String toString() {
        String res="";
        res+=exps.get(0).toString();
        for(int i=1;i<exps.size();i++){
            res+=commas.get(i-1).toString();
            res+=exps.get(i).toString();
        }
        res+="<FuncRParams>\n";
        return res;
    }
}
