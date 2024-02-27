package dao.nodes;

import java.util.List;

public class CompUnitNode {
    private List<DeclNode> decls;
    private List<FuncDefNode> funcDefs;
    private MainFuncDefNode mainFuncDef;

    public CompUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.decls = declNodes;
        this.funcDefs = funcDefNodes;
        this.mainFuncDef = mainFuncDefNode;
    }

    public List<DeclNode> getDecls() {
        return decls;
    }

    public List<FuncDefNode> getFuncDefs() {
        return funcDefs;
    }

    public MainFuncDefNode getMainFuncDef() {
        return mainFuncDef;
    }

    @Override
    public String toString() {
        String res="";
        for(DeclNode decl:decls){
            res+=decl.toString();
        }
        for(FuncDefNode funcDef:funcDefs){
            res+=funcDef.toString();
        }
        res+=mainFuncDef.toString();
        res+="<CompUnit>\n";
        return res;
    }
}
