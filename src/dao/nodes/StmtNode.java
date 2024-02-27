package dao.nodes;

import dao.Token;

import java.util.List;

public class StmtNode {
    // Stmt -> LVal '=' Exp ';'
    //	| [Exp] ';'
    //	| Block
    //	| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //	| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    //	| 'break' ';' | 'continue' ';'
    //	| 'return' [Exp] ';'
    //	| LVal '=' 'getint' '(' ')' ';'
    //	| 'printf' '(' FormatString { ',' Exp } ')' ';'
    public enum StmtType {
        LValAssignExp, Exp, Block, If, For, Break, Continue, Return, LValAssignGetint, Printf
    }

    private StmtType type;
    private LValNode LVal;
    private Token assignToken;
    private ExpNode exp;
    private Token semicolon;
    private BlockNode blockNode;
    private Token ifToken;
    private Token lParent;
    private Token forSemicolon1;
    private ForStmtNode forStmt1;
    private ForStmtNode forStmt2;
    private CondNode cond;
    private Token rParent;
    private List<StmtNode> stmts;
    private Token elseToken;
    private Token forToken;
    private Token breakOrContinueToken;
    private Token returnToken;
    private Token getintToken;
    private Token printfToken;
    private Token formatString;
    private List<Token> commas;
    private List<ExpNode> exps;

    public StmtNode(StmtType type, LValNode LVal, Token assignToken, ExpNode exp, Token semicolon) {
        // LVal '=' Exp ';'
        this.type = type;
        this.LVal = LVal;
        this.assignToken = assignToken;
        this.exp = exp;
        this.semicolon = semicolon;
    }

    public StmtNode(StmtType type, ExpNode exp, Token semicolon) {
        // [Exp] ';'
        this.type = type;
        this.exp = exp;
        this.semicolon = semicolon;
    }

    public StmtNode(StmtType type, BlockNode blockNode) {
        // Block
        this.type = type;
        this.blockNode = blockNode;
    }

    public StmtNode(StmtType type, Token ifToken, Token lParent, CondNode cond, Token rParent, List<StmtNode> stmts, Token elseToken) {
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        this.type = type;
        this.ifToken = ifToken;
        this.lParent = lParent;
        this.cond = cond;
        this.rParent = rParent;
        this.stmts = stmts;
        this.elseToken = elseToken;
    }

    public StmtNode(StmtType type, Token forToken, Token lParent, ForStmtNode forStmt1,Token semicolon,ForStmtNode forStmt2,Token forSemicolon1, CondNode cond, Token rParent, List<StmtNode> stmts) {
        //  'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        this.type = type;
        this.forToken = forToken;
        this.lParent = lParent;
        this.forStmt1 = forStmt1;
        this.forStmt2 = forStmt2;
        this.semicolon = semicolon;
        this.forSemicolon1 = forSemicolon1;
        this.cond = cond;
        this.rParent = rParent;
        this.stmts = stmts;
    }

    public StmtNode(StmtType type, Token breakOrContinueToken, Token semicolon) {
        // 'break' ';'
        this.type = type;
        this.breakOrContinueToken = breakOrContinueToken;
        this.semicolon = semicolon;
    }

    public StmtNode(StmtType type, Token returnToken, ExpNode exp, Token semicolon) {
        // 'return' [Exp] ';'
        this.type = type;
        this.returnToken = returnToken;
        this.exp = exp;
        this.semicolon = semicolon;
    }

    public StmtNode(StmtType type, LValNode LVal, Token assignToken, Token getintToken, Token lParent, Token rParent, Token semicolon) {
        // LVal '=' 'getint' '(' ')' ';'
        this.type = type;
        this.LVal = LVal;
        this.assignToken = assignToken;
        this.getintToken = getintToken;
        this.lParent = lParent;
        this.rParent = rParent;
        this.semicolon = semicolon;
    }

    public StmtNode(StmtType type, Token printfToken, Token lParent, Token formatString, List<Token> commas, List<ExpNode> expNodes, Token rParent, Token semicolon) {
        // 'printf' '(' FormatString { ',' Exp } ')' ';'
        this.type = type;
        this.printfToken = printfToken;
        this.lParent = lParent;
        this.formatString = formatString;
        this.commas = commas;
        this.exps = expNodes;
        this.rParent = rParent;
        this.semicolon = semicolon;
    }

    public StmtType getType() {
        return type;
    }

    public Token getAssignToken() {
        return assignToken;
    }

    public ExpNode getExp() {
        return exp;
    }

    public Token getSemicolon() {
        return semicolon;
    }

    public BlockNode getBlock() {
        return blockNode;
    }

    public Token getIfToken() {
        return ifToken;
    }

    public Token getlParent() {
        return lParent;
    }

    public CondNode getCond() {
        return cond;
    }
    public ForStmtNode getForStmt1(){return forStmt1;}
    public ForStmtNode getForStmt2(){return forStmt2;}

    public Token getrParent() {
        return rParent;
    }

    public List<StmtNode> getStmts() {
        return stmts;
    }

    public Token getElseToken() {
        return elseToken;
    }

    public Token getForToken() {
        return forToken;
    }

    public Token getBreakOrContinueToken() {
        return breakOrContinueToken;
    }

    public Token getGetintToken() {
        return getintToken;
    }

    public Token getPrintfToken() {
        return printfToken;
    }

    public Token getFormatString() {
        return formatString;
    }

    public List<Token> getCommas() {
        return commas;
    }

    public List<ExpNode> getExps() {
        return exps;
    }

    public Token getReturnToken() {
        return returnToken;
    }

    public LValNode getLVal() {
        return LVal;
    }



    public Token getForSemicolon1() {
        return forSemicolon1;
    }

    @Override
    public String toString() {
        String res="";
        switch (type) {
            case For -> {
                res+= forToken.toString();
                res+= lParent.toString();
                if(forStmt1!=null){
                    res+= forStmt1.toString();
                }
                res+= forSemicolon1.toString();
                //System.out.println(res);
                if(cond!=null){
                    res+= cond.toString();
                }
                res+=semicolon.toString();
                if(forStmt2!=null){
                    res+= forStmt2.toString();
                }
                res+=rParent.toString();
                res+=stmts.get(0).toString();
            }
            case LValAssignExp -> {
                res+= LVal.toString();
                res+= assignToken.toString();
                res+= exp.toString();
                res+= semicolon.toString();
            }
            case LValAssignGetint -> {
                res+= LVal.toString();
                res+= assignToken.toString();
                res+= getintToken.toString();
                res+= lParent.toString();
                res+= rParent.toString();
                res+= semicolon.toString();
            }
            case Exp -> {
                if(exp!=null){
                    res+=exp.toString();
                }
                res+=semicolon.toString();
            }
            case Return -> {
                res+= returnToken.toString();
                if(exp!=null){
                    res+=exp.toString();
                }
                res+=semicolon.toString();
            }
            case Block -> res+= blockNode.toString();
            case Break, Continue -> {
                res+= breakOrContinueToken.toString();
                res+= semicolon.toString();
            }
            case Printf -> {
                res += printfToken.toString();
                res += lParent.toString();
                res += formatString.toString();
                if (!exps.isEmpty()) {
                    for (int i = 0; i < exps.size(); i++) {
                        res += commas.get(i).toString();
                        res += exps.get(i).toString();
                    }
                }
                res += rParent.toString();
                res += semicolon.toString();
            }
            case If -> {
                res += ifToken.toString();
                res += lParent.toString();
                res += cond.toString();
                res += rParent.toString();
                res += stmts.get(0).toString();
                if (elseToken != null) {
                    res += elseToken.toString();
                    res += stmts.get(1).toString();
                }
            }
        }
        res+="<Stmt>\n";
        return res;
    }
}
