package dao.nodes;

import dao.Token;

public class MainFuncDefNode {
    private Token intToken;
    private Token mainToken;
    private Token lParent;
    private Token rParent;
    private BlockNode block;

    public MainFuncDefNode(Token intToken, Token mainToken, Token lParent, Token rParent, BlockNode block) {
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lParent = lParent;
        this.rParent = rParent;
        this.block = block;
    }

    public Token getIntToken() {
        return intToken;
    }

    public Token getMainToken() {
        return mainToken;
    }

    public Token getlParent() {
        return lParent;
    }

    public Token getrParent() {
        return rParent;
    }

    public BlockNode getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return intToken.toString()+mainToken.toString()+lParent.toString()+rParent.toString()+block.toString()
                +"<MainFuncDef>\n";
    }
}
