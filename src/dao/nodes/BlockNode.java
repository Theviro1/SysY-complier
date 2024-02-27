package dao.nodes;

import dao.Token;

import java.util.List;

public class BlockNode {
    private Token lBrace;
    private List<BlockItemNode> blockItems;
    private Token rBrace;

    public BlockNode(Token lBrace, List<BlockItemNode> blockItems, Token rBrace) {
        this.lBrace = lBrace;
        this.blockItems = blockItems;
        this.rBrace = rBrace;
    }

    public Token getlBrace() {
        return lBrace;
    }

    public List<BlockItemNode> getBlockItems() {
        return blockItems;
    }

    public Token getrBrace() {
        return rBrace;
    }

    @Override
    public String toString() {
        String res = lBrace.toString();
        for (BlockItemNode blockItem : blockItems) {
            res += blockItem.toString();
        }
        res += rBrace.toString();
        res += "<Block>\n";
        return res;
    }
}
