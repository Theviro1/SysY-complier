package dao.nodes;

import dao.Token;

import java.util.List;

public class FuncFParamsNode {
    private List<FuncFParamNode> funcFParams;
    private List<Token> commas;

    public FuncFParamsNode(List<FuncFParamNode> funcFParams, List<Token> commas) {
        this.funcFParams = funcFParams;
        this.commas = commas;
    }

    public List<FuncFParamNode> getFuncFParams() {
        return funcFParams;
    }

    public List<Token> getCommas() {
        return commas;
    }

    @Override
    public String toString() {
        String res="";
        res+=funcFParams.get(0);
        for(int i=1;i<funcFParams.size();i++){
            res+=commas.get(i-1).toString();
            res+=funcFParams.get(i).toString();
        }
        res+="<FuncFParams>\n";
        return res;
    }
}
