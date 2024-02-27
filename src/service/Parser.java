package service;

import controller.SymbolTable;
import dao.LexType;
import dao.Token;
import dao.error.Error;
import dao.error.ErrorType;
import dao.nodes.*;
import dao.symbol.TableEntry;
import dao.symbol.types.ArrayType;
import dao.symbol.types.FunctionType;
import dao.symbol.types.Type;
import dao.symbol.types.VarType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private Token token;
    private final Lexer lexer;
    private static final Parser parser=new Parser();

    /*错误处理使用*/
    private List<SymbolTable> symbolTableList;
    private List<Error> errorList;
    private Type t;/*向下传递的类型继承属性*/
    private Type ft;/*向下传递的类型继承属性*/
    private List<Integer> pl;/*向上传递的行数数组综合属性*/
    private List<Type> pt;/*向上传递的类型数组综合属性*/
    private List<String> pn;/*向上传递的名称数组综合属性*/
    private Integer l;/*向下传递的行号继承属性*/
    private Type r;/*向上传递的类型综合属性*/
    private Boolean hasReturn;/*判断函数是否存在返回值*/
    private Boolean returned;/*判断函数内是否产生了返回语句*/
    private Boolean isLoop;/*判断当前状态是否是循环*/

    //构造函数
    private Parser(){
        lexer=Lexer.getLexer();
        token=new Token();
        //错误处理：初始化
        symbolTableList = new ArrayList<>();
        errorList = new ArrayList<>();
        hasReturn = false;
        isLoop = false;
        pt = new ArrayList<>();
        pn = new ArrayList<>();
        pl = new ArrayList<>();
    }
    //getter & setter
    public static Parser getParser(){return  parser;}/*获取单例*/
    public List<Error> getErrorList(){return this.errorList;}

    //语法分析使用
    /*识别Exp的FIRST集合*/
    private boolean isExp(Token token){
        /*判断这个token是否是语法Exp的任意起始符号*/
        return token.type.equals(LexType.IDENFR.name()) ||
                token.type.equals(LexType.PLUS.name()) ||
                token.type.equals(LexType.MINU.name()) ||
                token.type.equals(LexType.NOT.name()) ||
                token.type.equals(LexType.LPARENT.name()) ||
                token.type.equals(LexType.INTCON.name());
    }
    //错误处理使用
    /*符号表入栈*/
    private void pushSymbolTable(SymbolTable symbolTable){
        symbolTableList.add(symbolTable);
    }
    /*符号表出栈*/
    private void popSymbolTable(){
        symbolTableList.remove(symbolTableList.size()-1);
    }
    /*向前查找全体符号表中是否存在*/
    private TableEntry findSymbol(String name){
        TableEntry res=null;
        int len = symbolTableList.size();
        for(int i = len-1; i >= 0; i--){
            res = symbolTableList.get(i).findSymbol(name);
            if(res!=null) break;
        }
        return res;
    }
    /*查找当前符号表中是否有重名*/
    private Boolean duplicateSymbol(String name){
        return symbolTableList.get(symbolTableList.size()-1).findSymbol(name)!=null;
    }
    /*给当前符号表添加一个符号*/
    private void addSymbol(String name, Boolean isConst, Type type){
        /*任意操作一定是对最顶层的符号表进行的，所以直接加到顶层符号表内*/
        symbolTableList.get(symbolTableList.size()-1).addSymbol(name,isConst,type,null);
    }

    //递归下降
    public CompUnitNode CompUnit(){
        /*错误处理：添加第一个全局范围的符号表*/
        pushSymbolTable(new SymbolTable());

        List<DeclNode> decls=new ArrayList<>();
        List<FuncDefNode> funcDefs=new ArrayList<>();
        MainFuncDefNode mainFuncDef;
        /*先判断Decl，通过判断：如果不是main，并且不是funcDef*/
        while(!lexer.preRead(1).type.equals(LexType.MAINTK.name()) && !lexer.preRead(2).type.equals(LexType.LPARENT.name())){
            DeclNode decl = Decl();
            decls.add(decl);
        }
        /*再判断FuncDef，通过判断：不是main*/
        while(!lexer.preRead(1).type.equals(LexType.MAINTK.name())){
            FuncDefNode funcDef = FuncDef();
            funcDefs.add(funcDef);
        }
        /*剩下就是main*/
        mainFuncDef = MainFuncDef();
        return new CompUnitNode(decls, funcDefs, mainFuncDef);
    }
    private DeclNode Decl(){
        ConstDeclNode constDecl=null;
        VarDeclNode varDecl=null;
        /*通过第一个单词是否是const来判断是ConstDecl还是VarDecl*/
        if(lexer.preRead(0).type.equals(LexType.CONSTTK.name())){
            constDecl = ConstDecl();
        }else {
            varDecl = VarDecl();
        }
        return new DeclNode(constDecl, varDecl);
    }
    private ConstDeclNode ConstDecl(){
        Token constToken=null;
        BTypeNode bType=null;
        List<ConstDefNode> constDefs=new ArrayList<>();
        List<Token> commas=new ArrayList<>();
        Token semicolon=null;
        /*  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'  */
        /*判别字符const*/
        lexer.next();
        token=lexer.getToken();
        Integer lineNum;
        if(token.type.equals(LexType.CONSTTK.name())){
            constToken=token;
            /*判别BType*/
            lexer.next();
            token=lexer.getToken();
            if(token.type.equals(LexType.INTTK.name())){
                t = VarType.i32;//错误处理：填写符号表的类型
                bType=new BTypeNode(token);
                /*判别ConstDef*/
                constDefs.add(ConstDef());/*必然至少有一个*/
                while(lexer.preRead(0).type.equals(LexType.COMMA.name())){/*如果获取到逗号*/
                    lexer.next();
                    token=lexer.getToken();
                    commas.add(token);
                    constDefs.add(ConstDef());/*调用ConstDef*/
                }
                /*判别句尾分号;*/
                //错误处理：句尾是否有分号
                lineNum = lexer.getLineNum();
                if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                    lexer.next();
                    token=lexer.getToken();
                    semicolon=token;
                }else {
                    Error error = new Error(lineNum, ErrorType.i);
                    errorList.add(error);
                }
            }else {
                System.out.println("ConstDecl token error:unable to find 'BType'");
            }

        }else {
            System.out.println("ConstDecl token error:unable to find 'const'");
        }
        return new ConstDeclNode(constToken,bType,commas,constDefs,semicolon);
    }
    private ConstDefNode ConstDef(){
        Token ident=null;
        List<Token> lBrackets=new ArrayList<>();
        List<ConstExpNode> constExps=new ArrayList<>();
        List<Token> rBrackets=new ArrayList<>();
        Token equal=null;
        ConstInitValNode constInitVal=null;

        /* ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal */
        /*判别标识符ident*/
        lexer.next();
        token=lexer.getToken();
        ident=token;
        //错误处理：判断变量名称是否已经定义
        Integer lineNum = lexer.getLineNum();
        if(duplicateSymbol(ident.token)){
            Error error = new Error(lineNum,ErrorType.b);
            errorList.add(error);
        }
        /*循环判别ConstExp*/
        lexer.next();
        token=lexer.getToken();
        List<Integer> dimensions = new ArrayList<>();//错误处理：获取数组维度
        while(token.type.equals(LexType.LBRACK.name())){
            dimensions.add(0);//错误处理：数组维度+1，具体信息不需要给出，以0代替
            lBrackets.add(token);
            constExps.add(ConstExp());/*调用ConstExp*/
            //错误处理：是否缺少右侧中括号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.RBRACK.name())){
                lexer.next();
                token=lexer.getToken();
                rBrackets.add(token);
            }
            else {
                Error error = new Error(lineNum,ErrorType.k);
                errorList.add(error);
            }
            lexer.next();
            token=lexer.getToken();/*移动到下一个'['或者'='并获取*/
        }
        //错误处理：填写符号表
        Type type = t;
        if(!dimensions.isEmpty()) type = new ArrayType(type,dimensions);
        addSymbol(ident.token,true, type);
        /*判别=符号*/
        if(token.type.equals(LexType.ASSIGN.name())){
            equal=token;
            /*判别ConstInitVal*/
            constInitVal=ConstInitVal();
        }else {
            System.out.println("ConstDef token error:unable to find '='");
        }
        return new ConstDefNode(ident,lBrackets,constExps,rBrackets,equal,constInitVal);
    }
    private ConstInitValNode ConstInitVal(){
        /* ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' */
        ConstExpNode constExp=null;
        Token lBrace=null;
        List<ConstInitValNode> constInitVals=new ArrayList<>();
        List<Token> commas=new ArrayList<>();
        Token rBrace=null;
        /*判断二者的区别在于是否以'{'起始*
        /*后者'{' [ ConstInitVal { ',' ConstInitVal } ] '}'*/
        if(lexer.preRead(0).type.equals(LexType.LBRACE.name())){
            lexer.next();
            token=lexer.getToken();
            lBrace=token;
            /*判别ConstInitVal*/
            constInitVals.add(ConstInitVal());/*至少有一个*/
            lexer.next();
            token=lexer.getToken();/*移动到下一个','或者'}'*/
            while(token.type.equals(LexType.COMMA.name())){
                commas.add(token);
                constInitVals.add(ConstInitVal());/*调用ConstInitVal*/
                lexer.next();
                token=lexer.getToken();/*移动到下一个','或者'}'*/
            }
            /*判别'}'*/
            if(token.type.equals(LexType.RBRACE.name())){
                rBrace=token;
            }else {
                System.out.println("ConstInitVal token error:unable to find '}'");
            }
        }
        /*前者ConstExp*/
        else {
            constExp=ConstExp();
        }
        return new ConstInitValNode(constExp,lBrace,constInitVals,commas,rBrace);
    }
    private ConstExpNode ConstExp(){
        return new ConstExpNode(AddExp());
    }
    private AddExpNode AddExp(){
        /* AddExp → MulExp | AddExp ('+' | '−') MulExp */
        /*这是一个左递归文法，直接按逻辑编程会进入死循环，所以这里化为右递归，可以发现最终AddExp左侧一定是由MulExp结尾，最终形式是MulExp ('+' | '−') MulExp ('+' | '−') MulExp ... 并且至少有1个MulExp，所以分析第一个MulExp
        * 改写为 AddExp → MulExp AddExp, AddExp → AddExp ('+' | '−') MulExp | NULL
        * */
        MulExpNode mulExp = MulExp();
        /*分析之后再去判断是否是+ -号，如果是的话就说明还在向后递归，再次调用AddExp()即可*/
        Token operator = null;
        AddExpNode addExp = null;
        /*判断下一个是否是+ -号*/
        if(lexer.preRead(0).type.equals(LexType.PLUS.name()) || lexer.preRead(0).type.equals(LexType.MINU.name())){
            lexer.next();
            token=lexer.getToken();
            operator=token;
            addExp=AddExp();
        }
        return new AddExpNode(mulExp, addExp, operator);
    }
    private MulExpNode MulExp(){
        /*  MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp */
        UnaryExpNode unaryExp = UnaryExp();
        Token operator = null;
        MulExpNode mulExp = null;
        /*判断下一个是否是* / %符号*/
        if(lexer.preRead(0).type.equals(LexType.MULT.name()) ||
           lexer.preRead(0).type.equals(LexType.DIV.name())  ||
           lexer.preRead(0).type.equals(LexType.MOD.name()) ){
            lexer.next();
            token=lexer.getToken();
            operator=token;
            mulExp=MulExp();
        }
        return new MulExpNode(unaryExp, mulExp, operator);
    }
    private UnaryExpNode UnaryExp(){
        /* UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp */
        /*判断是中间情况*/
        if(lexer.preRead(0).type.equals(LexType.IDENFR.name()) &&
           lexer.preRead(1).type.equals(LexType.LPARENT.name())){
            //System.out.println("UnaryExp get a func usage");

            /*获取标识符ident*/
            lexer.next();
            token=lexer.getToken();
            Token ident = token;
            //错误处理：ident是否已经被定义过，定义过则获取到类型
            Integer _l = l;
            Type _ft = ft;
            l = lexer.getLineNum();//错误处理：ident函数名的lineNum，向下继承
            if(findSymbol(ident.token)==null){
                Error error = new Error(l,ErrorType.c);
                errorList.add(error);
            }
            else ft = findSymbol(ident.token).type;//错误处理：l、ft继承到FuncRParams里
            /*获取左括号(*/
            lexer.next();
            token=lexer.getToken();
            Token lParent = token;
            /*判断是否有FuncRParams*/
            FuncRParamsNode funcRParams=null;
            Token rParent=null;
            if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                //错误处理：判断没有参数是否和函数参数个数相同
                if(findSymbol(ident.token)!=null){/*为空前面已经判定过，这里不需要额外处理，假设非空即可*/
                    if(!((FunctionType)ft).getParamsType().isEmpty()){
                        Error error = new Error(l,ErrorType.d);
                        errorList.add(error);
                    }
                }
                lexer.next();
                token=lexer.getToken();
                rParent=token;
            }else if (isExp(lexer.preRead(0))){
                funcRParams=FuncRParams();
                //错误处理：判断是否缺少右侧小括号
                Integer lineNum = lexer.getLineNum();
                if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                    lexer.next();
                    token=lexer.getToken();
                    rParent=token;
                }
                else {
                    Error error = new Error(lineNum,ErrorType.j);
                    errorList.add(error);
                }
            }else {
                //错误处理：判断是否缺少右侧小括号
                Integer lineNum = lexer.getLineNum();
                Error error = new Error(lineNum,ErrorType.j);
                errorList.add(error);
            }
            //错误处理：返回当前函数调用的结果类型
            if(ft==null) r = VarType._void;
            else r = ((FunctionType)ft).getReturnType();
            l = _l;
            ft = _ft;
            return new UnaryExpNode(ident,lParent,funcRParams,rParent);
        }
        /*判断是右边情况*/
        else if(lexer.preRead(0).type.equals(LexType.MINU.name()) ||
                lexer.preRead(0).type.equals(LexType.PLUS.name()) ||
                lexer.preRead(0).type.equals(LexType.NOT.name())){
            /*获取UnaryOp*/
            UnaryOpNode unaryOp = UnaryOp();
            UnaryExpNode unaryExp = UnaryExp();
            return new UnaryExpNode(unaryOp, unaryExp);
        }
        /*余下的是最左边情况*/
        else {
            PrimaryExpNode primaryExp = PrimaryExp();
            return new UnaryExpNode(primaryExp);
        }
    }
    private UnaryOpNode UnaryOp(){
        Token operator;
        lexer.next();
        token=lexer.getToken();
        operator=token;
        return new UnaryOpNode(operator);
    }
    private FuncRParamsNode FuncRParams(){
        /* FuncRParams → Exp { ',' Exp } */
        List<Token> commas = new ArrayList<>();
        List<ExpNode> exps = new ArrayList<>();
        exps.add(Exp());/*至少有一个Exp*/
        //错误处理：获取exp对应的type
        List<Type> types = new ArrayList<>();
        Type type;
        type = r;
        types.add(type);
        while(lexer.preRead(0).type.equals(LexType.COMMA.name())){
            /*获取逗号*/
            lexer.next();
            token=lexer.getToken();
            commas.add(token);
            /*获取exp*/
            exps.add(Exp());
            //错误处理：添加r
            type = r;
            types.add(type);
        }
        if(ft!=null){/*如果ft是null就说明这个符号本身就没定义，不需要在分析报错了*/
            //错误处理：参数个数不匹配
            System.out.println(((FunctionType)ft).getParamsType().size()+" "+exps.size());
            if(((FunctionType)ft).getParamsType().size()!=exps.size()){
                Error error = new Error(l,ErrorType.d);
                errorList.add(error);
            }else {
                //错误处理：参数类型不匹配
                for(int i = 0;i < exps.size();i++){
                    Type t1 = ((FunctionType) ft).getParamsType().get(i);
                    Type t2 = types.get(i);
                    if(!t1.equals(t2)){
                        Error error = new Error(l,ErrorType.e);
                        errorList.add(error);
                    }
                }
                /*如果本身参数就是空的根本就不会进入FuncRParams*/
            }
        }
        return new FuncRParamsNode(exps, commas);
    }
    private PrimaryExpNode PrimaryExp(){
        /* PrimaryExp → '(' Exp ')' | LVal | Number */
        /*Exp，先判断是否是最左边情况，通过是否以(开始判断*/
        if(lexer.preRead(0).type.equals(LexType.LPARENT.name())){
            /*获取左括号*/
            Token lParent;
            lexer.next();
            token=lexer.getToken();
            lParent=token;
            /*获取Exp*/
            ExpNode exp = Exp();
            /*获取右括号*/
            //错误处理：判断是否有右括号
            Integer lineNum = lexer.getLineNum();
            Token rParent=null;
            if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                lexer.next();
                token=lexer.getToken();
                rParent=token;
            }
            else {
                Error error = new Error(lineNum,ErrorType.j);
                errorList.add(error);
            }
            return new PrimaryExpNode(lParent,exp,rParent);
        }
        /*Number就是INTCON*/
        else if(lexer.preRead(0).type.equals(LexType.INTCON.name())){
            NumberNode number = Number();
            return new PrimaryExpNode(number);
        }
        /*LVal*/
        else {
            LValNode lVal = LVal();
            return new PrimaryExpNode(lVal);
        }
    }
    private NumberNode Number(){
        /* Number → IntConst */
        lexer.next();
        token=lexer.getToken();
        r = VarType.i32;//错误处理：获取底层类型
        return new NumberNode(token);
    }
    private LValNode LVal(){
        /* LVal → Ident {'[' Exp ']'} */
        Token ident;
        List<Token> lBrackets = new ArrayList<>();
        List<Token> rBrackets = new ArrayList<>();
        List<ExpNode> exps = new ArrayList<>();
        /*获取ident标识符*/
        lexer.next();
        token=lexer.getToken();
        ident=token;
        //错误处理：名字是否未定义
        Integer lineNum = lexer.getLineNum();
        if(findSymbol(ident.token)==null){
            Error error = new Error(lineNum,ErrorType.c);
            errorList.add(error);
        }
        int cnt = 0;
        /*循环判断[Exp]*/
        while(lexer.preRead(0).type.equals(LexType.LBRACK.name())){
            cnt++;//错误处理：判断部分数组
            /*获取左括号*/
            lexer.next();
            token=lexer.getToken();
            lBrackets.add(token);
            /*获取exp*/
            exps.add(Exp());
            /*获取右括号*/
            //错误处理：是否有右侧中括号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.RBRACK.name())){
                lexer.next();
                token=lexer.getToken();
                rBrackets.add(token);
            }
            else {
                Error error = new Error(lineNum,ErrorType.k);
                errorList.add(error);
            }
        }
        //错误处理：获取到底层类型
        if(findSymbol(ident.token)==null){/*前面已经报错处理过了*/
            r = VarType._void;
        }else {
            if(cnt!=0&&findSymbol(ident.token).type instanceof ArrayType){/*部分数组需要重新获取类型*/
                if(findSymbol(ident.token).type instanceof ArrayType){
                    int d = ((ArrayType)findSymbol(ident.token).type).dimension()-cnt;
                    if(d < 0){
                        r = VarType._void;/*例如对2维数组调用3维本身就是错误的，但是课程组没有给出这种错误，这里只能认为是类型错误*/
                    }else if(d==0){
                        r = ((ArrayType)findSymbol(ident.token).type).getType();
                    }else {
                        List<Integer> dimensions = new ArrayList<>();
                        for(int i=0;i<d;i++){
                            dimensions.add(0);
                        }
                        r = new ArrayType(((ArrayType)findSymbol(ident.token).type).getType(),dimensions);
                    }
                }
                else {/*对普通对象进行数组类型的调用本身就是错误的，但是课程组没有给出这种错误，这里只能认为是类型错误*/
                    r = VarType._void;
                }
            }else {
                r = findSymbol(ident.token).type;
                System.out.println(r);
            }
        }
        return new LValNode(ident, lBrackets, exps, rBrackets);
    }
    private ExpNode Exp(){
        return new ExpNode(AddExp());
    }
    private FuncDefNode FuncDef(){
        /*  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block */
        /*先获取FuncType*/
        FuncTypeNode funcType = FuncType();
        Token ident=null;
        Token lParent=null;
        FuncFParamsNode funcFParams=null;
        Token rParent=null;
        BlockNode block=null;
        /*获取Ident*/
        lexer.next();
        token=lexer.getToken();
        ident=token;
        //错误处理：重定义名称
        Integer lineNum = lexer.getLineNum();
        if(duplicateSymbol(ident.token)){
            Error error = new Error(lineNum,ErrorType.b);
            errorList.add(error);
        }
        /*获取(*/
        lexer.next();
        token=lexer.getToken();
        lParent=token;
        /*判断FuncFParams*/
        pt.clear();//错误处理：清空参数列表
        pn.clear();//错误处理：清空参数列表
        if (!lexer.preRead(0).type.equals(LexType.RPARENT.name())) {
            //错误处理：特殊情况，缺少右侧小括号的错误会被误认为是FuncFParams情况
            if(!lexer.preRead(0).type.equals(LexType.LBRACE.name())) funcFParams = FuncFParams();
        }
        //错误处理：函数名称全局符号表并生成新的符号表
        VarType returnType = funcType.getFuncType().token.equals("int")?VarType.i32:VarType._void;
        List<Type> paramsType = new ArrayList<>(pt);
        addSymbol(ident.token,false,new FunctionType(returnType,paramsType));
        pushSymbolTable(new SymbolTable());
        for(int i=0;i<pt.size();i++){
            if(duplicateSymbol(pn.get(i))){
                Error error = new Error(pl.get(i),ErrorType.b);
                errorList.add(error);
            }
            addSymbol(pn.get(i),false,pt.get(i));
        }
        /*获取)*/
        //错误处理：缺少右侧小括号
        lineNum = lexer.getLineNum();
        if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
            lexer.next();
            token=lexer.getToken();
            rParent=token;
        }
        else {
            Error error = new Error(lineNum,ErrorType.j);
            errorList.add(error);
        }
        /*判断Block*/
        //错误处理：判断返回值是否正确
        hasReturn = !funcType.getFuncType().token.equals("void");
        returned = false;
        block = Block();
        //错误处理：有返回值的函数没有return
        lineNum = lexer.getLineNum();
        if(hasReturn&&!returned){
            Error error = new Error(lineNum,ErrorType.g);
            errorList.add(error);
        }
        popSymbolTable();//错误处理：弹出符号表

        return new FuncDefNode(funcType,ident,lParent,funcFParams,rParent,block);
    }
    private FuncTypeNode FuncType(){
        /* FuncType → 'void' | 'int' */
        lexer.next();
        token=lexer.getToken();
        Token tk=token;
        return new FuncTypeNode(tk);
    }
    private FuncFParamsNode FuncFParams(){
        /* FuncFParams → FuncFParam { ',' FuncFParam } */
        List<FuncFParamNode> funcFParams = new ArrayList<>();
        List<Token> commas=new ArrayList<>();
        /*至少有一个FuncFParam*/
        funcFParams.add(FuncFParam());
        /*循环判断重复情况*/
        while(lexer.preRead(0).type.equals(LexType.COMMA.name())){
            lexer.next();
            token=lexer.getToken();
            commas.add(token);
            /*重复FuncFParam*/
            funcFParams.add(FuncFParam());
        }
        return new FuncFParamsNode(funcFParams, commas);
    }
    private FuncFParamNode FuncFParam(){
         /* FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] */
        BTypeNode bType=null;
        Token ident=null;
        List<Token> lBrackets = new ArrayList<>();
        List<Token> rBrackets = new ArrayList<>();
        List<ConstExpNode> constExps = new ArrayList<>();
        /*判断BType*/
        lexer.next();
        token=lexer.getToken();
        if(token.type.equals(LexType.INTTK.name())){
            bType=new BTypeNode(token);
            /*判断Ident*/
            lexer.next();
            token=lexer.getToken();
            ident=token;
            pl.add(lexer.getLineNum());//错误处理：添加参数所在行数
            pn.add(ident.token);//错误处理：添加参数名称
            List<Integer> dimensions = new ArrayList<>();//错误处理：获取数组维度
            /*循环判断是否有[][ConstExp]*/
            if(lexer.preRead(0).type.equals(LexType.LBRACK.name())){
                dimensions.add(0);//错误处理：以0替代维度
                /*获取左括号*/
                lexer.next();
                token=lexer.getToken();
                lBrackets.add(token);
                /*获取右括号*/
                //错误处理：缺少右侧中括号
                Integer lineNum = lexer.getLineNum();
                if(lexer.preRead(0).type.equals(LexType.RBRACK.name())){
                    lexer.next();
                    token=lexer.getToken();
                    rBrackets.add(token);
                }else {
                    Error error = new Error(lineNum,ErrorType.k);
                    errorList.add(error);
                }
                /*循环判断*/
                while(lexer.preRead(0).type.equals(LexType.LBRACK.name())){
                    dimensions.add(0);//错误处理：以0替代维度
                    /*获取左括号*/
                    lexer.next();
                    token=lexer.getToken();
                    lBrackets.add(token);
                    /*获取ConstExp表达式*/
                    constExps.add(ConstExp());
                    /*获取右括号*/
                    //错误处理：缺少右侧中括号
                    lineNum = lexer.getLineNum();
                    if(lexer.preRead(0).type.equals(LexType.RBRACK.name())){
                        lexer.next();
                        token=lexer.getToken();
                        rBrackets.add(token);
                    }else {
                        Error error = new Error(lineNum,ErrorType.k);
                        errorList.add(error);
                    }
                }
            }
            //错误处理：添加参数
            /*这里的BType只有i32类型，所以直接添加i32*/
            if(!dimensions.isEmpty()) pt.add(new ArrayType(VarType.i32,dimensions));
            else pt.add(VarType.i32);
        }else {
            System.out.println("FuncFParam token error:unable to find BType");
        }
        return new FuncFParamNode(bType,ident,lBrackets,rBrackets,constExps);
    }
    private BlockNode Block(){
        /* Block → '{' { BlockItem } '}' */
        Token lBrace=null;
        List<BlockItemNode> blockItems=new ArrayList<>();
        Token rBrace;
        /*判断左括号*/
        lexer.next();
        token=lexer.getToken();
        if(token.type.equals(LexType.LBRACE.name())){
            lBrace=token;
        }else {
            System.out.println("Block token error:unable to find '{'");
        }
        /*循环判断BlockItem*/
        while(!lexer.preRead(0).type.equals(LexType.RBRACE.name())){
            blockItems.add(BlockItem());
        }
        /*判断右括号*/
        lexer.next();
        token=lexer.getToken();
        rBrace=token;
        return new BlockNode(lBrace,blockItems,rBrace);
    }
    private BlockItemNode BlockItem(){
        /* BlockItem → Decl | Stmt */
        DeclNode decl=null;
        StmtNode stmt=null;
        /*判断是否是Decl就看开头是否是const或者int*/
        if(lexer.preRead(0).type.equals(LexType.INTTK.name())||
           lexer.preRead(0).type.equals(LexType.CONSTTK.name())){
            decl = Decl();
            return new BlockItemNode(decl);
        }
        else {
            stmt=Stmt();
            return new BlockItemNode(stmt);
        }
    }
    private StmtNode Stmt(){
        /* Stmt → LVal '=' Exp ';'
                | [Exp] ';'
                | Block
                | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                | 'break' ';' | 'continue' ';'
                | 'return' [Exp] ';'
                | LVal '=' 'getint''('')'';'
                | 'printf''('FormatString{','Exp}')'';' */
        token=lexer.preRead(0);
        /*判断Block*/
        if(token.type.equals(LexType.LBRACE.name())){
            //System.out.println(1);
            pushSymbolTable(new SymbolTable());//错误处理：添加新的符号表
            BlockNode block = Block();
            popSymbolTable();//错误处理：弹出符号表
            return new StmtNode(StmtNode.StmtType.Block, block);
        }
        /*判断if*/
        else if(token.type.equals(LexType.IFTK.name())){
            Token ifTk=null;
            Token lParent=null;
            CondNode cond=null;
            Token rParent=null;
            List<StmtNode> stmts=new ArrayList<>();
            Token elseTk=null;
            /*if*/
            lexer.next();
            token=lexer.getToken();
            ifTk=token;
            /*(*/
            lexer.next();
            token=lexer.getToken();
            lParent=token;
            /*Cond*/
            cond = Cond();
            /*)*/
            //错误处理：缺少右侧小括号
            Integer lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                lexer.next();
                token=lexer.getToken();
                rParent=token;
            }else {
                Error error = new Error(lineNum,ErrorType.j);
                errorList.add(error);
            }

            /*Stmt*/
            stmts.add(Stmt());
            /*else Stmt*/
            if(lexer.preRead(0).type.equals(LexType.ELSETK.name())){
                lexer.next();
                token=lexer.getToken();
                elseTk=token;

                stmts.add(Stmt());
            }
            return new StmtNode(StmtNode.StmtType.If,ifTk,lParent,cond,rParent,stmts,elseTk);
        }
        /*判断break*/
        else if(token.type.equals(LexType.BREAKTK.name())){
            Token breakOrContinueTk=null;
            Token semicolon=null;
            /*break或continue*/
            lexer.next();
            token=lexer.getToken();
            breakOrContinueTk=token;
            //错误处理：非循环语句
            Integer lineNum = lexer.getLineNum();
            if(!isLoop){
                Error error = new Error(lineNum,ErrorType.m);
                errorList.add(error);
            }
            /*;*/
            //错误处理：缺少分号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                lexer.next();
                token=lexer.getToken();
                semicolon=token;
            }else {
                Error error = new Error(lineNum,ErrorType.i);
                errorList.add(error);
            }
            return new StmtNode(StmtNode.StmtType.Break, breakOrContinueTk,semicolon);
        }
        /*判断continue*/
        else if(token.type.equals(LexType.CONTINUETK.name())){
            Token breakOrContinueTk=null;
            Token semicolon=null;
            /*break或continue*/
            lexer.next();
            token=lexer.getToken();
            breakOrContinueTk=token;
            //错误处理：非循环语句
            Integer lineNum = lexer.getLineNum();
            if(!isLoop){
                Error error = new Error(lineNum,ErrorType.m);
                errorList.add(error);
            }
            /*;*/
            //错误处理：缺少分号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                lexer.next();
                token=lexer.getToken();
                semicolon=token;
            }else {
                Error error = new Error(lineNum,ErrorType.i);
                errorList.add(error);
            }
            return new StmtNode(StmtNode.StmtType.Continue, breakOrContinueTk,semicolon);
        }
        /*判断return*/
        else if(token.type.equals(LexType.RETURNTK.name())){
            Token returnTk;
            ExpNode exp=null;
            Token semicolon=null;
            /*return*/
            lexer.next();
            token=lexer.getToken();
            returnTk=token;
            //错误处理：没有返回值的语句出现了返回值
            Integer lineNum = lexer.getLineNum();
            if(!lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                exp = Exp();
                returned = true;
                if(!hasReturn){
                    Error error = new Error(lineNum,ErrorType.f);
                    errorList.add(error);
                }
            }
            /*;*/
            //错误处理：缺少分号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                lexer.next();
                token=lexer.getToken();
                semicolon=token;
            }else {
                Error error = new Error(lineNum,ErrorType.i);
                errorList.add(error);
            }
            return new StmtNode(StmtNode.StmtType.Return, returnTk,exp,semicolon);
        }
        /*判断printf*/
        else if(token.type.equals(LexType.PRINTFTK.name())){
            Token printfTk;
            Token lParent;
            Token formatString;
            List<Token> commas=new ArrayList<>();
            List<ExpNode> exps=new ArrayList<>();
            Token rParent=null;
            Token semicolon=null;
            /*printf*/
            lexer.next();
            token=lexer.getToken();
            printfTk=token;
            Integer printfLineNum = lexer.getLineNum();
            /*(*/
            lexer.next();
            token=lexer.getToken();
            lParent=token;
            /*formatString*/
            lexer.next();
            token=lexer.getToken();
            formatString=token;
            //错误处理：是否存在非法字符
            String str = formatString.token.substring(1,formatString.token.length()-1);/*去掉双引号*/
            Integer lineNum = lexer.getLineNum();
            int cnt = 0;/*总的%d个数*/
            boolean flag = false;
            for(int i=0;i<str.length();i++){
                int c = str.charAt(i);
                if((i+1)<str.length()&&c=='%'&&str.charAt(i+1)=='d'){
                    cnt++;
                    i++;
                }
                else if(c!=32&&c!=33&&!(c>=40&&c<=126)){
                    flag = true;
                }
                else if(c=='\\'){
                    if(!((i+1)<str.length()&&str.charAt(i+1)=='n')){
                        flag = true;
                    }
                }
            }
            if(flag){
                Error error = new Error(lineNum,ErrorType.a);
                errorList.add(error);
            }
            /*循环判断是否有Exp*/
            while (lexer.preRead(0).type.equals(LexType.COMMA.name())){
                /*,*/
                lexer.next();
                token=lexer.getToken();
                commas.add(token);
                /*Exp*/
                exps.add(Exp());
            }
            //错误处理：判断表达式和字符串内%d个数是否相同
            if(exps.size()!=cnt){
                Error error = new Error(printfLineNum,ErrorType.l);
                errorList.add(error);
            }
            /*)*/
            //错误处理：缺少右侧小括号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                lexer.next();
                token=lexer.getToken();
                rParent=token;
            }else {
                Error error = new Error(lineNum,ErrorType.j);
                errorList.add(error);
            }
            /*;*/
            //错误处理：缺少分号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                lexer.next();
                token=lexer.getToken();
                semicolon=token;
            }else {
                Error error = new Error(lineNum,ErrorType.i);
                errorList.add(error);
            }
            return new StmtNode(StmtNode.StmtType.Printf,printfTk,lParent,formatString,commas,exps,rParent,semicolon);
        }
        /*判断for*/
        else if(token.type.equals(LexType.FORTK.name())){
            //错误处理：设置isLoop状态
            Boolean flag = isLoop;
            isLoop = true;

            Token forTk;
            Token lParent;
            ForStmtNode forStmt1=null;
            ForStmtNode forStmt2=null;
            Token semicolon1;
            CondNode cond=null;
            Token semicolon2;
            Token rParent;
            List<StmtNode> stmts=new ArrayList<>();
            /*for*/
            lexer.next();
            token=lexer.getToken();
            forTk=token;
            /*(*/
            lexer.next();
            token=lexer.getToken();
            lParent=token;
            //System.out.println(1);
            /*判断是否有ForStmt*/
            if(!lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                forStmt1 = ForStmt();
            }
            lexer.next();
            token=lexer.getToken();
            semicolon1=token;
            //System.out.println("111"+forStmt1);
            /*判断是否有cond*/
            if(!lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                cond = Cond();
            }
            lexer.next();
            token=lexer.getToken();
            semicolon2=token;
            //System.out.println(semicolon2);
            /*判断是否有ForStmt*/
            if(!lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                forStmt2=ForStmt();
            }
            lexer.next();
            token=lexer.getToken();
            rParent=token;
            //System.out.println(rParent);
            /*Stmt*/
            stmts.add(Stmt());
            isLoop = flag;//错误处理：isLoop置为原状态
            return new StmtNode(StmtNode.StmtType.For,forTk,lParent,forStmt1,semicolon1,forStmt2,semicolon2,cond,rParent,stmts);
        }
        /*判断LVal和Exp*/
        else{
            boolean isExp = true;
            Token tmpTk=token;
            /*分析判断可以得到，最终实际上LVal的开头一定是Ident，而Exp的开头只有一个Ident，这个Ident之后一定有一个(
            * 所以如果这个表达式以Ident开头并且第二个符号不是(就一定是LVal开头，可以先parse一个LVal之后再判断后面是不是 =
            * 如果是=那就一定是LVal的那个等式，否则就是Exp*/
            if(token.type.equals(LexType.IDENFR.name()) && !lexer.preRead(1).type.equals(LexType.LPARENT.name())){
                Integer pos = lexer.getCurPos();
                Integer line = lexer.getLineNum();
                List<Error> errors = new ArrayList<>(errorList);
                LVal();
                errorList = new ArrayList<>(errors);
                /*判断下一个是否是等号*/
                if(lexer.preRead(0).type.equals(LexType.ASSIGN.name())){
                    isExp = false;/*是等号就说明是LVal的*/
                }
                /*回溯*/
                lexer.setCurPos(pos);
                lexer.setLineNum(line);
            }
            /*处理Exp*/
            if(isExp){
                ExpNode exp = null;
                if(isExp(tmpTk)) exp=Exp();
                //System.out.println("this is stmt->exp:\n");
                /*判断分号*/
                Token semicolon = null;
                //错误处理：缺少分号
                Integer lineNum = lexer.getLineNum();
                if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                    lexer.next();
                    token=lexer.getToken();
                    semicolon=token;
                }else {
                    Error error = new Error(lineNum,ErrorType.i);
                    errorList.add(error);
                }
                return new StmtNode(StmtNode.StmtType.Exp, exp, semicolon);
            }
            /*处理LVal*/
            else {
                LValNode lVal = LVal();
                //错误处理：不能修改常量的值
                Integer lineNum = lexer.getLineNum();
                /*变量未定义在LVal内已经报错过了，但是这里findSymbol还有可能是null，需要判断*/
                if(findSymbol(lVal.getIdent().token)!=null&&findSymbol(lVal.getIdent().token).isConst){
                    Error error = new Error(lineNum,ErrorType.h);
                    errorList.add(error);
                }
                /*判断等号*/
                lexer.next();
                token=lexer.getToken();
                Token assign=token;
                /*判断getint*/
                if(lexer.preRead(0).type.equals(LexType.GETINTTK.name())){
                    /*getint*/
                    Token getintTk;
                    Token lParent;
                    Token rParent=null;
                    Token semicolon=null;
                    lexer.next();
                    token=lexer.getToken();
                    getintTk=token;
                    /*(*/
                    lexer.next();
                    token=lexer.getToken();
                    lParent=token;
                    /*)*/
                    //错误处理：缺少右侧小括号
                    lineNum = lexer.getLineNum();
                    if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
                        lexer.next();
                        token=lexer.getToken();
                        rParent=token;
                    }else {
                        Error error = new Error(lineNum,ErrorType.j);
                        errorList.add(error);
                    }
                    /*;*/
                    //错误处理：缺少分号
                    lineNum = lexer.getLineNum();
                    if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                        lexer.next();
                        token=lexer.getToken();
                        semicolon=token;
                    }else {
                        Error error = new Error(lineNum,ErrorType.i);
                        errorList.add(error);
                    }
                    return new StmtNode(StmtNode.StmtType.LValAssignGetint,lVal,assign,getintTk,lParent,rParent,semicolon);
                }
                /*LVal = Exp*/
                else {
                    ExpNode exp = Exp();
                    /*;*/
                    Token semicolon = null;
                    //错误处理：缺少分号
                    lineNum = lexer.getLineNum();
                    if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
                        lexer.next();
                        token=lexer.getToken();
                        semicolon=token;
                    }else {
                        Error error = new Error(lineNum,ErrorType.i);
                        errorList.add(error);
                    }
                    return new StmtNode(StmtNode.StmtType.LValAssignExp,lVal,assign,exp,semicolon);
                }
            }
        }
    }
    private ForStmtNode ForStmt(){
        LValNode LVal = LVal();
        //错误处理：是否对常量进行了更改
        Integer lineNum = lexer.getLineNum();
        if(findSymbol(LVal.getIdent().token).isConst){
            Error error = new Error(lineNum, ErrorType.h);
            errorList.add(error);
        }
        lexer.next();
        token=lexer.getToken();
        Token assign=token;
        ExpNode exp = Exp();
        return new ForStmtNode(LVal,assign,exp);
    }
    private CondNode Cond(){
        /* Cond → LOrExp */
        return new CondNode(LOrExp());
    }
    private LOrExpNode LOrExp(){
        /* LOrExp → LAndExp | LOrExp '||' LAndExp */
        LAndExpNode lAndExp = LAndExp();
        Token operator = null;
        LOrExpNode lOrExp = null;
        /*判断||*/
        if(lexer.preRead(0).type.equals(LexType.OR.name())){
            lexer.next();
            token=lexer.getToken();
            operator=token;
            lOrExp = LOrExp();
        }
        return new LOrExpNode(lAndExp, operator, lOrExp);
    }
    private LAndExpNode LAndExp(){
        /*  LAndExp → EqExp | LAndExp '&&' EqExp */
        EqExpNode eqExp = EqExp();
        Token operator = null;
        LAndExpNode lAndExp = null;
        /*判断&&*/
        if(lexer.preRead(0).type.equals(LexType.AND.name())){
            lexer.next();
            token=lexer.getToken();
            operator=token;
            lAndExp = LAndExp();
        }
        return new LAndExpNode(eqExp, operator, lAndExp);
    }
    private EqExpNode EqExp(){
        /* EqExp → RelExp | EqExp ('==' | '!=') RelExp */
        RelExpNode relExp = RelExp();
        Token operator = null;
        EqExpNode eqExp = null;
        /*判断==和!=*/
        if(lexer.preRead(0).type.equals(LexType.EQL.name()) || lexer.preRead(0).type.equals(LexType.NEQ.name())){
            lexer.next();
            token = lexer.getToken();
            operator=token;
            eqExp = EqExp();
        }
        return new EqExpNode(relExp, operator, eqExp);
    }
    private RelExpNode RelExp(){
        /* RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp */
        AddExpNode addExp = AddExp();
        Token operator = null;
        RelExpNode relExp = null;
        /*判断> < <= >=*/
        if( lexer.preRead(0).type.equals(LexType.LSS.name()) ||
            lexer.preRead(0).type.equals(LexType.LEQ.name()) ||
            lexer.preRead(0).type.equals(LexType.GRE.name()) ||
            lexer.preRead(0).type.equals(LexType.GEQ.name())){
            lexer.next();
            token = lexer.getToken();
            operator = token;
            relExp = RelExp();
        }
        return new RelExpNode(addExp, operator, relExp);
    }
    private VarDeclNode VarDecl(){
        /*  VarDecl → BType VarDef { ',' VarDef } ';' */
        BTypeNode bType;
        List<VarDefNode> varDefs = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token semicolon=null;
        /*判断BType*/
        lexer.next();
        token=lexer.getToken();
        bType=new BTypeNode(token);
        t = VarType.i32;//错误处理：符号表内类型，向下继承
        /*循环判断VarDef*/
        varDefs.add(VarDef());
        while (lexer.preRead(0).type.equals(LexType.COMMA.name())){
            lexer.next();
            token = lexer.getToken();
            commas.add(token);
            varDefs.add(VarDef());
        }
        /*判断;*/
        //错误处理：句尾是否存在分号
        Integer lineNum = lexer.getLineNum();
        if(lexer.preRead(0).type.equals(LexType.SEMICN.name())){
            lexer.next();
            token = lexer.getToken();
            semicolon=token;
        }else {
            Error error = new Error(lineNum,ErrorType.i);
            errorList.add(error);
        }
        return new VarDeclNode(bType, varDefs, commas, semicolon);
    }
    private VarDefNode VarDef(){
        /*  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal */
        Token ident;
        List<Token> lBrackets=new ArrayList<>();
        List<Token> rBrackets=new ArrayList<>();
        List<ConstExpNode> constExps=new ArrayList<>();
        Token assign=null;
        InitValNode initVal=null;
        /*前边的公共部分一定会有的，所以先循环判断*/
        /*判断ident*/
        lexer.next();
        token=lexer.getToken();
        ident=token;
        //错误处理：ident是否已经定义
        Integer lineNum = lexer.getLineNum();
        if(duplicateSymbol(ident.token)){
            Error error = new Error(lineNum,ErrorType.b);
            errorList.add(error);
        }
        /*循环判断ConstExp*/
        List<Integer> dimensions = new ArrayList<>();//错误处理：获取变量维度
        while (lexer.preRead(0).type.equals(LexType.LBRACK.name())){
            dimensions.add(0);//错误处理：获取维度，具体内容使用0替代
            /*左括号*/
            lexer.next();
            token=lexer.getToken();
            lBrackets.add(token);
            /*ConstExp*/
            constExps.add(ConstExp());
            /*右括号*/
            //错误处理：判断是否有右侧中括号
            lineNum = lexer.getLineNum();
            if(lexer.preRead(0).type.equals(LexType.RBRACK.name())){
                lexer.next();
                token=lexer.getToken();
                rBrackets.add(token);
            }else {
                Error error = new Error(lineNum,ErrorType.k);
                errorList.add(error);
            }
        }
        //错误处理：填写符号表
        Type type = t;
        if(!dimensions.isEmpty()) type = new ArrayType(type,dimensions);
        addSymbol(ident.token, false,type);
        /*判断是否还有等号*/
        if(lexer.preRead(0).type.equals(LexType.ASSIGN.name())){
            lexer.next();
            token = lexer.getToken();
            assign=token;
            initVal=InitVal();
        }
        return new VarDefNode(ident, lBrackets, constExps, rBrackets, assign, initVal);
    }
    private InitValNode InitVal(){
        /* InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}' */
        ExpNode exp=null;
        Token lBrace=null;
        List<InitValNode> initVals = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token rBrace=null;
        /*判断是Exp还是{*/
        if(lexer.preRead(0).type.equals(LexType.LBRACE.name())){
            /*{*/
            lexer.next();
            token=lexer.getToken();
            lBrace=token;
            /*判断是否是}还是InitVal*/
            if(!lexer.preRead(0).type.equals(LexType.RBRACE.name())){
                initVals.add(InitVal());/*至少有一个*/
                /*循环判断后面有几个*/
                while (lexer.preRead(0).type.equals(LexType.COMMA.name())){
                    lexer.next();
                    token=lexer.getToken();
                    commas.add(token);
                    initVals.add(InitVal());
                }

            }
            /*获取右括号*/
            lexer.next();
            token=lexer.getToken();
            rBrace=token;
            return new InitValNode(lBrace,initVals,commas,rBrace);
        }
        else {
            exp=Exp();
            return new InitValNode(exp);
        }
    }
    private MainFuncDefNode MainFuncDef(){
        /*  MainFuncDef → 'int' 'main' '(' ')' Block */
        Token intTk;
        Token mainTk;
        Token lParent;
        Token rParent=null;
        BlockNode block;
        /*'int'*/
        lexer.next();
        token=lexer.getToken();
        intTk=token;
        /*'main'*/
        lexer.next();
        token=lexer.getToken();
        mainTk=token;
        /*'('*/
        lexer.next();
        token=lexer.getToken();
        lParent=token;
        /*')'*/
        //错误处理：缺少右侧小括号
        Integer lineNum = lexer.getLineNum();
        if(lexer.preRead(0).type.equals(LexType.RPARENT.name())){
            lexer.next();
            token=lexer.getToken();
            rParent=token;
        }else {
            Error error = new Error(lineNum,ErrorType.j);
            errorList.add(error);
        }
        addSymbol(mainTk.token, false,VarType.i32);//错误处理：添加main到全局符号表内
        pushSymbolTable(new SymbolTable());//错误处理：添加新的符号表
        //错误处理：设置当前函数是否存在返回值
        hasReturn = true;
        returned = false;
        block=Block();
        lineNum = lexer.getLineNum();
        if(!returned){
            Error error = new Error(lineNum,ErrorType.g);
            errorList.add(error);
        }
        popSymbolTable();//错误处理：弹出符号表
        return new MainFuncDefNode(intTk,mainTk,lParent,rParent,block);
    }
}
