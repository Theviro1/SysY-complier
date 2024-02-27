package service;

import controller.Calculator;
import dao.IR.IRBuilder;
import dao.IR.instructions.global.Func;
import dao.IR.instructions.global.Global;
import dao.IR.instructions.terminator.Call;
import dao.IR.nodes.BasicBlock;
import dao.IR.nodes.Function;
import dao.IR.nodes.IRModule;
import dao.IR.regs.Reg;
import dao.LexType;
import dao.Token;
import dao.nodes.*;
import dao.symbol.TableEntry;
import dao.symbol.types.*;

import java.util.ArrayList;
import java.util.List;

public class Generator {
    private Boolean isConst;
    private Boolean isArray;
    private BasicBlock curBlock;/*当前指向的BasicBlock*/
    private Function curFunction;/*当前指向的Function*/
    private IRBuilder irBuilder;/*辅助指令构造器*/
    private IRModule irModule = IRModule.getIRModule();/*单例的LLVM总模块*/

    private static final Generator generator = new Generator();
    /*需要用到的继承属性和综合属性*/
    private String tmpOperator;/*符号：继承属性*/
    private Integer tmpValue;/*结果数值：综合属性*/
    private Reg tmpReg;/*结果寄存器：综合属性*/
    private Type tmpDownType;/*指令类型：继承属性*//*用于变量定义时的向下传递生成指令*/
    private Type tmpUpType;/*指令类型：综合属性*//*用于分析语句时由底层类型向上传递获取到语句类型*/
    private List<Type> tmpParamsType;/*函数参数类型列表：综合属性*/
    private List<String> tmpParamsName;/*函数参数名称列表：综合属性*/
    private List<Reg> tmpOperand;/*栈式函数参数列表：综合属性和继承属性*/
    private List<Integer> tmpInitVals;/*全局数组初始化值列表：综合属性*/
    private List<Reg> tmpInitRegs;/*局部数组初始化值列表：综合属性*/
    private BasicBlock tmpTrue;/*if for语句中需要用到的当前的条件为真对应的基本块：继承属性*/
    private BasicBlock tmpFalse;/*if for语句中需要用到的当前的条件为假对应的基本块：继承属性*/
    private Reg tmpBreak;/*for语句中标记break的跳转地址标签寄存器：继承属性*/
    private Reg tmpContinue;/*for语句中标记continue的跳转地址标签寄存器：继承属性*/
    private Boolean jump=false;/*代码块中是否出现break和continue：综合属性*/
    /*私有构造函数*/
    private Generator(){
        this.irBuilder = new IRBuilder();
        this.tmpParamsName = new ArrayList<>();
        this.tmpParamsType = new ArrayList<>();
        this.tmpOperand = new ArrayList<>();
        this.tmpInitRegs = new ArrayList<>();
        this.tmpInitVals = new ArrayList<>();
        GlobalFunction();
    }
    /*获取生成器单例*/
    public static Generator getGenerator(){return generator;}
    public IRModule getIrModule(){return this.irModule;}


    /*存入全局变量*/
    public void GlobalFunction(){
        /*getint全局函数*/
        List<Type> getintparamsType = new ArrayList<>();
        Reg getintReg = new Reg("getint",true);
        irModule.symbolTable.addSymbol("getint",null,new FunctionType(VarType.i32,getintparamsType),getintReg);
        irBuilder.buildDeclare(irModule,getintReg,new FunctionType(VarType.i32,getintparamsType));
        /*putint全局函数*/
        List<Type> putintParamsType = new ArrayList<>();
        Reg putintReg = new Reg("putint",true);
        putintParamsType.add(VarType.i32);
        irModule.symbolTable.addSymbol("putint",null,new FunctionType(VarType._void,putintParamsType),putintReg);
        irBuilder.buildDeclare(irModule,putintReg,new FunctionType(VarType._void,putintParamsType));
        /*putch全局函数*/
        List<Type> putchParamsType = new ArrayList<>();
        putchParamsType.add(VarType.i32);
        Reg putchReg = new Reg("putch",true);
        irModule.symbolTable.addSymbol("putch",null,new FunctionType(VarType._void,putchParamsType),putchReg);
        irBuilder.buildDeclare(irModule,putchReg,new FunctionType(VarType._void,putintParamsType));
        /*getch全局函数*/
        List<Type> getchParamsType = new ArrayList<>();
        Reg getchReg = new Reg("getch",true);
        irModule.symbolTable.addSymbol("getch",null,new FunctionType(VarType.i32,getchParamsType),getchReg);
        irBuilder.buildDeclare(irModule,getchReg,new FunctionType(VarType.i32,getchParamsType));
        /*getarray putarray全局函数*/
        List<Type> putstrParamsType = new ArrayList<>();
        putstrParamsType.add(new PointerType(VarType.i8));
        Reg putstrReg = new Reg("putstr",true);
        irModule.symbolTable.addSymbol("putstr",null,new FunctionType(VarType._void,putstrParamsType),putstrReg);
        irBuilder.buildDeclare(irModule,putstrReg,new FunctionType(VarType._void,putstrParamsType));
    }

    /*递归遍历语法树*/
    public void visitCompUnit(CompUnitNode compUnit){
        //CompUnit → {Decl} {FuncDef} MainFuncDef
        this.isArray = false;
        this.isConst = true;/*一开始所有Decl全部都是全局的*/

        for(DeclNode decl: compUnit.getDecls()){
            visitDecl(decl);
        }
        for(FuncDefNode funcDef: compUnit.getFuncDefs()){
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(compUnit.getMainFuncDef());
    }
    public void visitDecl(DeclNode decl){
        //Decl → ConstDecl | VarDecl
        if(decl.getConstDecl()!=null){
            visitConstDecl(decl.getConstDecl());
        }else {
            visitVarDecl(decl.getVarDecl());
        }
    }
    public void visitConstDecl(ConstDeclNode constDecl){
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        tmpDownType = VarType.i32;/*根据BType得到的类型，BType只能是int*/
        for(ConstDefNode constDef:constDecl.getConstDefs()){
            visitConstDef(constDef);
        }
    }
    public void visitConstDef(ConstDefNode constDef){
        // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
        String identName = constDef.getIdent().token;/*获取变量名称*/
        Type thisDownType = tmpDownType;
        if(constDef.getConstExps().isEmpty()){
            /*非数组变量*/
            visitConstInitVal(constDef.getConstInitVal());
            if(isConst){
                Integer thisValue = tmpValue;/*全局变量获取到的是值*/
                /*构造Global指令*/
                Reg reg = new Reg(identName,true);
                Global global = new Global(reg,thisDownType,true,thisValue);
                irModule.addGlobal(global);
                irModule.symbolTable.addSymbol(identName,true,thisDownType,reg,thisValue);/*带初始值的全局变量*/
            }else {
                Reg thisReg = tmpReg;/*非全局变量获取到的是寄存器*/
                /*构造普通常量指令：一条alloca指令以及一条store指令*/
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildAlloca(curBlock,reg,thisDownType);
                curBlock.symbolTable.addSymbol(identName,true,thisDownType,reg);/*局部变量*/
                irBuilder.buildStore(curBlock,thisReg,reg,thisDownType);
            }
        }
        else {
            List<Integer> dimensions = new ArrayList<>();
            Boolean flag = isConst;
            isConst = true;/*由于维度大小一定是常数，需要走isConst一路来计算值tmpValue，先置为true*/
            for(ConstExpNode constExp:constDef.getConstExps()){
                visitConstExp(constExp);
                Integer thisValue = tmpValue;/*先visitExp来获取到完整的维度信息，包括每一个维度的大小和总维度数量*/
                dimensions.add(thisValue);/*通过综合属性tmpValue完成访问，并保存到一个数组里*/
            }
            isConst = flag;
            if(isConst){/*全局数组常量*/
                ArrayType arrayType = new ArrayType(thisDownType,dimensions);/*数组类型*/
                Reg reg = new Reg(identName,true);/*数组名寄存器*/
                /*全局数组常量的初始值一定是常数*/
                tmpInitVals.clear();/*下降前清空初始值*/
                visitConstInitVal(constDef.getConstInitVal());
                List<Integer> thisInitVals = new ArrayList<>(tmpInitVals);
                /*直接构造全局数组指令即可*/
                Global global = new Global(reg,arrayType,true,thisInitVals);/*全局数组获取的一定是数值Value*/
                irModule.addGlobal(global);
                /*添加到符号表*/
                irModule.symbolTable.addSymbol(identName,true,arrayType,reg,thisInitVals);
            }
            else {/*局部数组常量*/
                /*构造基本信息*/
                Reg allocaReg = curBlock.regBuilder.build();/*结果寄存器*/
                ArrayType arrayType = new ArrayType(thisDownType,dimensions);
                irBuilder.buildAlloca(curBlock,allocaReg,arrayType);
                curBlock.symbolTable.addSymbol(identName,true,arrayType,allocaReg);/*添加到符号表*/
                /*下降访问初始值*/
                tmpInitRegs.clear();/*下降前清空初始值*/
                visitConstInitVal(constDef.getConstInitVal());/*局部数组可以用变量赋值，所以获取的是Reg不是Value*/
                List<Reg> thisInitRegs = new ArrayList<>(tmpInitRegs);
                /*由于文法规定数组每个元素一定有初始值即thisInitRegs.size=arrayType.capacity，所以直接遍历生成GEP指令*/
                for(int i = 0; i < thisInitRegs.size(); i++){
                    /*GEP和Store指令*/
                    Reg gepReg = curBlock.regBuilder.build();
                    irBuilder.buildGEP(curBlock,gepReg,allocaReg,i,arrayType);
                    irBuilder.buildStore(curBlock,thisInitRegs.get(i),gepReg,thisDownType);
                }
            }
        }
    }
    public void visitConstInitVal(ConstInitValNode constInitVal){
        // ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if(constInitVal.getConstExp()!=null && !isArray){
            /*非数组变量*/
            visitConstExp(constInitVal.getConstExp());
        }else {
            /*数组变量*/
            for(ConstInitValNode constInitValNode: constInitVal.getConstInitVals()){
                visitConstInitVal(constInitValNode);
                /*只有在下一个节点下面是Exp的时候才需要添加到数组内，否则上层的ConstInitVal会重复添加上一个tmp的值*/
                if(constInitValNode.getConstExp()!=null){
                    if(isConst) {/*全局数组添加数值*/
                        Integer thisValue = tmpValue;
                        tmpInitVals.add(thisValue);
                    }else {/*局部数组添加寄存器*/
                        Reg thisReg = tmpReg;
                        tmpInitRegs.add(thisReg);
                    }
                }
            }
            //System.out.println(1);
        }
    }
    public void visitVarDecl(VarDeclNode varDecl){
        // VarDecl -> BType VarDef { ',' VarDef } ';'
        tmpDownType = VarType.i32;
        for(VarDefNode varDef:varDecl.getVarDefs()){
            visitVarDef(varDef);
        }
    }
    public void visitVarDef(VarDefNode varDef){
        // VarDef -> Ident { '[' ConstExp ']' } [ '=' InitVal ]
        String identName = varDef.getIdent().token;
        Type thisDownType = tmpDownType;
        if(varDef.getConstExps().isEmpty()){/*非数组变量*/
            if(varDef.getInitVal()==null){/*没有初始值*/
                if(isConst){/*没有初始值的全局变量默认初始值是0*/
                    /*只需要生成Global指令*/
                    Reg reg = new Reg(identName,true);
                    Global global = new Global(reg,thisDownType,false,0);
                    irModule.symbolTable.addSymbol(identName,false,thisDownType,reg,0);
                    irModule.addGlobal(global);
                }
                else {/*没有初始值的局部变量*/
                    /*只需要生成Alloca指令，不需要Store*/
                    Reg reg = curBlock.regBuilder.build();
                    irBuilder.buildAlloca(curBlock,reg,thisDownType);
                    curBlock.symbolTable.addSymbol(identName,false,thisDownType,reg);
                }
            }
            else {/*有初始值*/
                visitInitVal(varDef.getInitVal());
                if(isConst){/*有初始值的全局变量*/
                    Integer thisValue = tmpValue;/*visitInitVal生成的综合属性*/
                    /*只需要生成全局指令*/
                    Reg reg = new Reg(identName,true);
                    Global global = new Global(reg,thisDownType,false,thisValue);
                    irModule.addGlobal(global);
                    irModule.symbolTable.addSymbol(identName,false,thisDownType,reg,thisValue);
                }else {/*有初始值的局部变量*/
                    Reg thisReg = tmpReg;
                    /*生成alloca和store指令*/
                    Reg reg = curBlock.regBuilder.build();
                    irBuilder.buildAlloca(curBlock,reg,thisDownType);
                    curBlock.symbolTable.addSymbol(identName,false,thisDownType,reg);
                    irBuilder.buildStore(curBlock,thisReg,reg,thisDownType);
                }
            }
        }
        else {/*数组变量*/
            List<Integer> dimensions = new ArrayList<>();
            /*访问数组维度*/
            Boolean flag = isConst;
            isConst = true;
            for(ConstExpNode constExp:varDef.getConstExps()){
                visitConstExp(constExp);
                Integer thisValue = tmpValue;
                dimensions.add(thisValue);
            }
            isConst = flag;
            if(varDef.getInitVal()==null){
                /*没有初始值*/
                if(isConst){/*没有初始值的全局数组默认初始值是0*/
                    /*生成一条global语句*/
                    Reg reg = new Reg(identName,true);
                    ArrayType arrayType = new ArrayType(thisDownType,dimensions);
                    Global global = new Global(reg,arrayType,false,null,true);
                    irModule.addGlobal(global);
                    irModule.symbolTable.addSymbol(identName,false,arrayType,reg);
                }
                else {/*没有初始值的局部数组*/
                    ArrayType arrayType = new ArrayType(thisDownType,dimensions);
                    Reg reg = curBlock.regBuilder.build();
                    /*只有一条alloca指令*/
                    irBuilder.buildAlloca(curBlock,reg,arrayType);
                    curBlock.symbolTable.addSymbol(identName,false,arrayType,reg);
                }
            }
            else {
                if(isConst){/*有初始值全局变量*/
                    //System.out.println("fuck");
                    /*访问初始值*/
                    Reg reg = new Reg(identName,true);
                    ArrayType arrayType = new ArrayType(thisDownType,dimensions);
                    /*递归下降*/
                    tmpInitVals.clear();
                    visitInitVal(varDef.getInitVal());
                    List<Integer> thisInitVals = new ArrayList<>(tmpInitVals);
                    /*直接构造全局指令*/
                    Global global = new Global(reg,arrayType,false,thisInitVals);
                    irModule.addGlobal(global);
                    irModule.symbolTable.addSymbol(identName,false,arrayType,reg,thisInitVals);
                }
                else {/*有初始值局部变量*/
                    /*构造alloca指令*/
                    Reg allocaReg = curBlock.regBuilder.build();
                    ArrayType arrayType = new ArrayType(thisDownType,dimensions);
                    irBuilder.buildAlloca(curBlock,allocaReg,arrayType);
                    curBlock.symbolTable.addSymbol(identName,false,arrayType,allocaReg);
                    /*递归下降*/
                    tmpInitRegs.clear();
                    visitInitVal(varDef.getInitVal());
                    List<Reg> thisInitRegs = new ArrayList<>(tmpInitRegs);
                    /*构造GEP和store指令*/
                    for(int i = 0 ;i < thisInitRegs.size(); i++){
                        /*GEP和Store指令*/
                        Reg gepReg = curBlock.regBuilder.build();
                        irBuilder.buildGEP(curBlock,gepReg,allocaReg,i,arrayType);
                        irBuilder.buildStore(curBlock,thisInitRegs.get(i),gepReg,thisDownType);
                    }
                }
            }
        }
    }
    public void visitInitVal(InitValNode initVal){
        // InitVal -> Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if(initVal.getExp()!=null && !isArray){
            /*非数组变量*/
            visitExp(initVal.getExp());
        }else {
            for(InitValNode initValNode:initVal.getInitVals()){
                visitInitVal(initValNode);
                if(initValNode.getExp()!=null){
                    if(isConst) {/*全局数组添加数值*/
                        Integer thisValue = tmpValue;
                        tmpInitVals.add(thisValue);
                    }else {/*局部数组添加寄存器*/
                        Reg thisReg = tmpReg;
                        tmpInitRegs.add(thisReg);
                    }
                }
            }
        }
    }
    public void visitFuncDef(FuncDefNode funcDef){
        // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
        isConst = false;/*关闭全局*/
        /*创建一个新的Function*/
        Function function = new Function();
        irModule.addFunction(function);
        curFunction = function;/*指针指向当前的函数*/
        /*分析语句中函数的基本信息*/
        String funcName = funcDef.getIdent().token;
        VarType returnType = funcDef.getFuncType().getFuncType().type.equals(LexType.INTTK.name())?VarType.i32:VarType._void;/*返回值类型只有i32和void两种，只需要使用三目运算符判断是否是其中一种即可*/
        tmpParamsType.clear();
        tmpParamsName.clear();/*下降获取参数前先清空参数列表*/
        if(funcDef.getFuncFParams()!=null){
            visitFuncFParams(funcDef.getFuncFParams());
        }
        /*构造函数定义指令、存表*/
        /*Block部分，创建一个新的Block*/
        BasicBlock basicBlock = new BasicBlock();
        function.setBlock(basicBlock);
        /*基本信息的获取*/
        List<Reg> operand = new ArrayList<>();
        Reg funcNameReg = new Reg(funcName,true);/*函数名称的寄存器*/
        List<Type> thisParamsType = new ArrayList<>(tmpParamsType);
        List<String> thisParamsName = new ArrayList<>(tmpParamsName);
        FunctionType functionType = new FunctionType(returnType,thisParamsType);
        operand.add(funcNameReg);
        irModule.symbolTable.addSymbol(funcName,null,functionType,funcNameReg);/*函数添加到全局符号表*/
        /*指令的构造*/
        for(int i = 0;i < thisParamsType.size();i++){/*函数参数添加到函数符号表*/
            Reg reg = function.regBuilder.buildParam();/*创建一个函数参数寄存器*/
            operand.add(reg);/*写入寄存器到操作数里用于生成指令*/
            function.symbolTable.addSymbol(thisParamsName.get(i),false,thisParamsType.get(i),reg);/*存入函数符号表*/
            /*llvm的语法中要求函数内部调用形参的时候必须先alloca新寄存器并store进去调用*/
            /*Alloca指令*/
            Reg allocaReg = basicBlock.regBuilder.build();
            irBuilder.buildAlloca(basicBlock,allocaReg,thisParamsType.get(i));
            /*Store指令*/
            irBuilder.buildStore(basicBlock,reg,allocaReg,thisParamsType.get(i));
            basicBlock.symbolTable.addSymbol(thisParamsName.get(i),false,thisParamsType.get(i),allocaReg);
        }
        Func func = new Func(operand,functionType);/*构造函数指令*/
        function.setFunctionInstruction(func);
        /*继续向后访问Block*/
        curBlock = basicBlock;/*指针指向新的基本块*/
        visitBlock(funcDef.getBlock());
        /*如果函数是void类型就添加一条ret void，重复添加是不会错的，不是void类型的函数一定有return，否则会语法错误*/
        if(returnType==VarType._void) irBuilder.buildRet(curBlock);
        curBlock = null;/*访问结束Block结束*/

        isConst = true;/*恢复全局*/
    }
    public void visitMainFuncDef(MainFuncDefNode mainFuncDef){
        // MainFuncDef -> 'int' 'main' '(' ')' Block
        isConst = false;/*关闭全局*/
        /*创建一个新的Function*/
        Function function = new Function();
        irModule.addFunction(function);
        curFunction = function;
        /*构造主函数、存入符号表*/
        Reg funcNameReg = new Reg("main",true);
        FunctionType functionType = new FunctionType(VarType.i32,new ArrayList<>());
        irModule.symbolTable.addSymbol("main",null,functionType,funcNameReg);
        List<Reg> operand = new ArrayList<>();
        operand.add(funcNameReg);/*只需要函数名*/
        Func func = new Func(operand,functionType);/*构造函数指令*/
        function.setFunctionInstruction(func);
        /*继续向后处理Block部分，创建一个新的Block*/
        BasicBlock basicBlock = new BasicBlock();
        function.setBlock(basicBlock);
        curBlock = basicBlock;
        visitBlock(mainFuncDef.getBlock());
        curBlock = null;/*访问结束Block结束*/

        isConst = true;/*恢复全局*/
    }
    public void visitFuncFParams(FuncFParamsNode funcFParams){
        // FuncFParams -> FuncFParam { ',' FuncFParam }
        for(FuncFParamNode funcFParam:funcFParams.getFuncFParams()){
            visitFuncFParam(funcFParam);
        }
    }
    public void visitFuncFParam(FuncFParamNode funcFParam){
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        String identName = funcFParam.getIdent().token;
        if (funcFParam.getlBracket().isEmpty()) {/*非数组对象*/
            /*只需要获取到tmpParamsType和tmpParamsName两个属性即可，符号表由上一层处理*/
            Type paramType = VarType.i32;/*由BType得到，目前只有i32一种类型*/
            tmpParamsName.add(identName);
            tmpParamsType.add(paramType);
        }else {
            /*数组对象*/
            /*整体被当作一个指针类型，指针指向的类型是一个数组，相当于一个数组指针*/
            tmpParamsName.add(identName);
            /*访问ConstExp获取到维度*/
            Boolean flag = isConst;
            isConst = true;
            List<Integer> crd = new ArrayList<>();
            for(ConstExpNode constExp: funcFParam.getConstExps()){
                visitConstExp(constExp);
                Integer thisValue = tmpValue;
                crd.add(thisValue);
            }
            isConst = flag;
            /*构造指针数组*/
            ArrayType arrayType = new ArrayType(VarType.i32,crd);/*由BType得到，目前只有i32一种类型*/
            Type paramType = new PointerType(arrayType);
            tmpParamsType.add(paramType);
            //System.out.println(paramType);
        }
    }

    public void visitBlock(BlockNode block){
        // Block -> '{' { BlockItem } '}'
        for(BlockItemNode blockItem:block.getBlockItems()){
            visitBlockItem(blockItem);
        }
    }
    public void visitBlockItem(BlockItemNode blockItem){
        // BlockItem -> Decl | Stmt
        if(blockItem.getDecl()!=null) {
            visitDecl(blockItem.getDecl());
        }else {
            visitStmt(blockItem.getStmt());
        }
    }


    public void visitStmt(StmtNode stmt){
        /*注意stmt不可能在全局部分出现，一定是局部的*/
        /*Stmt → LVal '=' Exp ';'
               | [Exp] ';'
               | Block
               | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
               | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
               | 'break' ';' | 'continue' ';'
               | 'return' [Exp] ';'
               | LVal '=' 'getint''('')'';'
               | 'printf' '(' FormatString {','Exp} ')' ';'  */
        switch (stmt.getType()){
            /*第一种情况：左值表达式的赋值语句*/
            case LValAssignExp -> {
                String identName = stmt.getLVal().getIdent().token;
                TableEntry tableEntry = curBlock.findSymbol(identName);
                /*除了交给LVal内判断处理，需要在这里也判断一次左值是否是数组，从而判断需要用哪种指令和数据结构*/
                if(stmt.getLVal().getExps().isEmpty()){/*非数组变量*/
                    /*构造store指令*/
                    /*由于visitLVal内是在load找到的LVal寄存器的值进入新寄存器并传递上来，这里不能Load只能Store，所以不能visitLVal*/
                    visitExp(stmt.getExp());
                    Type thisUpType = tmpUpType;
                    Reg thisReg = tmpReg;
                    irBuilder.buildStore(curBlock,thisReg,tableEntry.reg,thisUpType);
                }else {/*数组变量*/
                    /*左值表达式是数组，也就是要对数组中的某一个元素进行赋值，需要讨论是否是函数参数，如果是函数参数首先需要调用一次load指令，然后调用一次GEP指令取值，再调用一次store命令赋值，左侧不存在数组调用，所以没有复杂的GEP指令*/
                    /*首先访问坐标，注意所有坐标都是常值，先把isConst置为true访问结束之后再置回false*/
                    List<Reg> crd = new ArrayList<>();
                    for(ExpNode exp:stmt.getLVal().getExps()){
                        visitExp(exp);
                        Reg thisReg = tmpReg;
                        crd.add(thisReg);
                    }
                    /*其次访问右侧表达式，获取到要赋值的变量寄存器*/
                    visitExp(stmt.getExp());
                    Reg thisReg = tmpReg;
                    Type thisUpType = tmpUpType;/*赋值类型*/
                    /*如果是调用函数参数*/
                    if(curFunction.symbolTable.findSymbol(identName)!=null){
                        /*构造Load指令*/
                        Reg loadReg = curBlock.regBuilder.build();
                        Type loadType = tableEntry.type;
                        irBuilder.buildLoad(curBlock,loadReg,tableEntry.reg, loadType);
                        loadType = ((PointerType)loadType).getTarget();/*load完指针变回到数组*/
                        /*构造GEP指令*/
                        /*先构造一条偏移GEP指令*/
                        Reg gepReg = curBlock.regBuilder.build();
                        irBuilder.buildGEP(curBlock,crd.get(0),gepReg,loadReg,loadType);
                        crd = new ArrayList<>(crd.subList(1,crd.size()));
                        if(!crd.isEmpty()){
                            Reg gepReg1 = curBlock.regBuilder.build();
                            irBuilder.buildGEP(curBlock,gepReg1,gepReg,crd,loadType);
                            gepReg = gepReg1;
                        }
                        /*构造store指令*/
                        irBuilder.buildStore(curBlock,thisReg,gepReg,thisUpType);
                    }
                    else {
                        /*对于一般调用直接构造一条GEP和一条Store*/
                        Reg gepReg = curBlock.regBuilder.build();
                        irBuilder.buildGEP(curBlock,gepReg,tableEntry.reg,crd, tableEntry.type);
                        irBuilder.buildStore(curBlock,thisReg,gepReg,thisUpType);
                    }
                }
            }
            case Exp -> {
                if(stmt.getExp()!=null){
                    visitExp(stmt.getExp());
                }
            }
            case Block -> {
                /*递归生成基本块*/
                BasicBlock basicBlock = new BasicBlock();
                basicBlock.setRegBuilder(curBlock.regBuilder);
                basicBlock.setPrev(curBlock);
                BasicBlock thisBlock = curBlock;/*记录当前Block*/
                curBlock = basicBlock;
                visitBlock(stmt.getBlock());
                curBlock = thisBlock;/*回到当前的Block内*/
                curBlock.addInstructionList(basicBlock.getInstructions());/*把新Block内的指令添加到当前Block内*/
            }
            case Return -> {
                /*构造一条Return指令*/
                jump = true;/*已经return了也相当于完成了一次跳转*/
                if(stmt.getExp()!=null){
                    visitExp(stmt.getExp());
                    Reg thisReg = tmpReg;
                    Type thisUpType = tmpUpType;
                    irBuilder.buildRet(curBlock,thisReg,thisUpType);
                }else {/*直接返回*/
                    irBuilder.buildRet(curBlock);
                }
            }
            case LValAssignGetint -> {
                String identName = stmt.getLVal().getIdent().token;
                TableEntry getintTableEntry = irModule.findSymbol("getint");/*getint函数的表项*/
                TableEntry lValTableEntry = curBlock.findSymbol(identName);/*左值表达式里符号的表项*/
                Reg lValReg = lValTableEntry.reg;/*左值符号对应的寄存器，需要被赋予函数计算结果的值*/
                if(stmt.getLVal().getExps().isEmpty()) {/*非数组变量*/
                    /*getint是有i32类型的返回值的，并且没有任何参数，构造一条Call指令以及一条Store指令*/
                    /*Call指令*/
                    Reg reg = curBlock.regBuilder.build();/*分配一个临时存储函数结果的寄存器*/
                    irBuilder.buildCall(curBlock,reg,getintTableEntry.reg,getintTableEntry.type);
                    /*Store指令*/
                    irBuilder.buildStore(curBlock,reg,lValReg,VarType.i32);/*getint是i32类型直接填写，实际应用returnType字段*/
                }else {/*数组变量*/
                    /*获取坐标*/
                    List<Reg> crd = new ArrayList<>();
                    for(ExpNode exp:stmt.getLVal().getExps()){
                        visitExp(exp);
                        Reg thisReg = tmpReg;
                        crd.add(thisReg);
                    }
                    /*先构造一条GEP指令，再构造一条Call指令和一条Store指令*/
                    /*GEP指令*/
                    Reg gepReg = curBlock.regBuilder.build();
                    irBuilder.buildGEP(curBlock,gepReg,lValTableEntry.reg,crd, lValTableEntry.type);
                    /*Call指令*/
                    Reg callReg = curBlock.regBuilder.build();
                    irBuilder.buildCall(curBlock,callReg,getintTableEntry.reg,getintTableEntry.type);
                    /*Store指令*/
                    irBuilder.buildStore(curBlock,callReg,gepReg,VarType.i32);/*getint是i32直接填写，实际应用returnType字段*/
                }
            }
            case Printf -> {
                String formatString = stmt.getFormatString().token.replace("\"","");/*去掉双引号*/
                //System.out.println(formatString);
                TableEntry putintTableEntry = irModule.findSymbol("putint");
                TableEntry putchTableEntry = irModule.findSymbol("putch");
                //System.out.println(((FunctionType)putchTableEntry.type).getParamsType().size());
                Reg putintReg = putintTableEntry.reg;
                Reg putchReg = putchTableEntry.reg;
                List<Reg> results = new ArrayList<>();
                for(ExpNode exp:stmt.getExps()){
                    visitExp(exp);/*先访问每一个表达式，获取到结果寄存器*/
                    Reg thisReg = tmpReg;
                    results.add(thisReg);/*全部存入到数组内*/
                }
                int resultIdx = 0;/*results的指针，用于一一对应result和%d*/
                for(int i = 0;i < formatString.length();i++){
                    char c = formatString.charAt(i);
                    //System.out.print(c+" ");
                    if(c == '%'){
                        /*调用putint输出对应的寄存器，putint是void类型*/
                        i++;/*下一位一定是d，直接跳过即可*/
                        List<Reg> operand = new ArrayList<>();
                        operand.add(putintReg);/*放入函数名称*/
                        operand.add(results.get(resultIdx));/*放入对应的exp计算结果*/
                        resultIdx++;/*更新指针*/
                        Call call = new Call(operand, putintTableEntry.type);/*putint*/
                        curBlock.addInstruction(call);
                    }
                    else if(c == '\\'&&formatString.charAt(i+1)=='n'){
                        i++;/*下一位是n就可以直接跳过*/
                        List<Reg> operand = new ArrayList<>();
                        operand.add(putchReg);
                        operand.add(new Reg(10));/*\n的ASCII码是10*/
                        Call call = new Call(operand, putchTableEntry.type);/*putch*/
                        curBlock.addInstruction(call);
                    }
                    else {
                        //System.out.print("normal char:");
                        /*一般字符，调用putch输出*/
                        List<Reg> operand = new ArrayList<>();
                        operand.add(putchReg);
                        operand.add(new Reg((int)c));/*直接输出ASCII码*/
                        //System.out.print(operand+" ");
                        Call call = new Call(operand,putchTableEntry.type);/*putch*/
                        //System.out.println(call);
                        curBlock.addInstruction(call);
                    }
                }
            }
            case If -> {
                /*只有if没有else*/
                Boolean thisJump = jump;
                jump = false;/*默认当前if内没有跳转语句*/
                if(stmt.getElseToken()==null){
                    BasicBlock thisBlock = curBlock;
                    /*建立需要的基本块*/
                    BasicBlock trueBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    BasicBlock finalBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    /*访问cond表达式，cond中一定是非空的，首先建立一个BasicBlock作为cond的起始LOr的block，构造一条跳转指令进入内部*/
                    BasicBlock condBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    irBuilder.buildBr(curBlock,condBlock.getLabel());/*跳转进入cond*/
                    /*设置继承属性*/
                    tmpTrue = trueBlock;
                    tmpFalse = finalBlock;
                    curBlock = condBlock;
                    visitCond(stmt.getCond());
                    thisBlock.addInstructionList(condBlock.getInstructions());
                    /*访问结束之后，继续访问trueBlock对应的stmt*/
                    curBlock = trueBlock;
                    visitStmt(stmt.getStmts().get(0));
                    //System.out.println("if: "+jump);
                    if(!jump) irBuilder.buildBr(curBlock,finalBlock.getLabel());/*在trueBlock最后添加一条跳转出口*/
                    thisBlock.addInstructionList(trueBlock.getInstructions());
                    /*指向出口*/
                    thisBlock.addInstructionList(finalBlock.getInstructions());/*里面应该只有一条标签*/
                    curBlock = thisBlock;
                }
                else {
                    BasicBlock thisBlock = curBlock;
                    /*建立需要的基本块*/
                    BasicBlock trueBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    BasicBlock falseBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    BasicBlock finalBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    /*访问cond表达式*/
                    BasicBlock condBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                    irBuilder.buildBr(curBlock,condBlock.getLabel());
                    /*设置继承属性*/
                    tmpTrue = trueBlock;
                    tmpFalse = falseBlock;
                    curBlock = condBlock;
                    visitCond(stmt.getCond());
                    thisBlock.addInstructionList(condBlock.getInstructions());
                    /*访问true和falseBlock对应的stmt*/
                    curBlock = trueBlock;
                    visitStmt(stmt.getStmts().get(0));
                    //System.out.println("if: "+jump);
                    if(!jump) irBuilder.buildBr(curBlock,finalBlock.getLabel());
                    thisBlock.addInstructionList(trueBlock.getInstructions());

                    jump = false;/*if和else是两个不同的块，是否jump也需要分开来，这里重新把jump置为false*/

                    curBlock = falseBlock;
                    visitStmt(stmt.getStmts().get(1));
                    //System.out.println("else: "+jump);
                    if(!jump) irBuilder.buildBr(curBlock,finalBlock.getLabel());
                    thisBlock.addInstructionList(falseBlock.getInstructions());
                    /*指向出口*/
                    thisBlock.addInstructionList(finalBlock.getInstructions());
                    curBlock = thisBlock;
                }
                jump = thisJump;
            }
            case For -> {
                /*for循环内部也是可以嵌套的，首先要记录break和continue的继承属性，在结束之后置回*/
                Boolean thisJump = jump;
                jump = false;/*默认当前For内部没有跳转语句*/
                /*'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt*/
                Reg thisBreak = tmpBreak;
                Reg thisContinue = tmpContinue;
                BasicBlock thisBlock = curBlock;
                /*建立需要的基本块*/
                BasicBlock condBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                BasicBlock trueBlock = irBuilder.buildCondBlock(curFunction,curBlock);
                BasicBlock finalBlock = irBuilder.buildCondBlock(curFunction,curBlock);/*工具块，内部只有一条标签*/
                BasicBlock forBlock2 = irBuilder.buildCondBlock(curFunction,curBlock);
                tmpBreak = finalBlock.getLabel();
                tmpContinue = forBlock2.getLabel();
                /*先分析ForStmt1部分*/
                BasicBlock forBlock1 = new BasicBlock();/*这个块不需要标签，直接归入之前的代码部分*/
                forBlock1.setPrev(thisBlock);
                forBlock1.setRegBuilder(thisBlock.regBuilder);
                curBlock = forBlock1;
                if(stmt.getForStmt1()!=null) visitForStmt(stmt.getForStmt1());
                irBuilder.buildBr(curBlock,condBlock.getLabel());/*从forStmt1跳转到cond内*/
                thisBlock.addInstructionList(forBlock1.getInstructions());
                /*设置继承属性，进入cond进行访问*/
                tmpTrue = trueBlock;
                tmpFalse = finalBlock;
                curBlock = condBlock;
                if(stmt.getCond()!=null) visitCond(stmt.getCond());/*非空则访问，短路求值*/
                else irBuilder.buildBr(curBlock,trueBlock.getLabel());/*为空则相当于1，是永真的*/
                thisBlock.addInstructionList(condBlock.getInstructions());
                /*stmt也就是trueBlock*/
                curBlock = trueBlock;/*block设置为trueBlock之后访问stmt，确认stmt的指令属于trueBlock*/
                visitStmt(stmt.getStmts().get(0));
                //System.out.println("for: "+jump);
                if(!jump) irBuilder.buildBr(curBlock,forBlock2.getLabel());/*从stmt跳转到forStmt2*/
                thisBlock.addInstructionList(trueBlock.getInstructions());
                /*forStmt2*/
                curBlock = forBlock2;
                if(stmt.getForStmt2()!=null) visitForStmt(stmt.getForStmt2());
                irBuilder.buildBr(curBlock,condBlock.getLabel());/*从forStmt2跳转到cond*/
                thisBlock.addInstructionList(forBlock2.getInstructions());
                /*指向出口*/
                thisBlock.addInstructionList(finalBlock.getInstructions());
                curBlock = thisBlock;
                tmpContinue = thisContinue;
                tmpBreak = thisBreak;

                jump = thisJump;
            }
            /*如果遇到了Break和Continue一定是在for语句内部，当前一定位于for的stmt语句内部*/
            case Break -> {
                Reg thisBreak = tmpBreak;
                irBuilder.buildBr(curBlock,thisBreak);/*当前if或for代码块中前面没有跳转才添加*/
                curBlock.lockInstructionList();/*保证break和continue后面其他命令都不生效*/
                jump = true;
            }
            case Continue -> {
                Reg thisContinue = tmpContinue;
                irBuilder.buildBr(curBlock,thisContinue);/*当前if或for代码块中前面没有跳转才添加*/
                curBlock.lockInstructionList();/*保证break和continue后面其他命令都不生效*/
                jump = true;
            }
            default -> {}
        }

    }
    public void visitForStmt(ForStmtNode forStmt){
        // ForStmt → LVal '=' Exp
        /*和visitStmt中的LValAssignExp是完全一样的*/
        StmtNode stmt = new StmtNode(StmtNode.StmtType.LValAssignExp,forStmt.getLVal(),forStmt.getEqual(),forStmt.getExp(),new Token(";",LexType.SEMICN.getType()));
        visitStmt(stmt);
    }

    public void visitConstExp(ConstExpNode constExp){
        // ConstExp -> AddExp
        /*ConstExp是顶层表达式，这里开始计算值，所以要先把用到的tmpValue和tmpReg、tmpUpType继承属性清零*/
        tmpValue = null;
        tmpReg = null;
        tmpUpType = VarType.i32;/*默认值为i32*/
        visitAddExp(constExp.getAddExp());
    }
    public void visitExp(ExpNode exp){
        // Exp -> AddExp
        /*Exp也是顶层表达式，这里开始计算值，所以下降前要先把用到的全部清零*/
        tmpValue = null;
        tmpReg = null;
        tmpUpType = VarType.i32;
        visitAddExp(exp.getAddExp());
    }
    public void visitAddExp(AddExpNode addExp){
        // AddExp -> MulExp | MulExp ('+' | '−') AddExp
        if(isConst){/*全局表达式需要计算得到结果，一定需要tmpValue*/
            Integer thisValue = tmpValue;
            String thisOperator = tmpOperator;
            tmpValue = null;/*下降前设置为null*/
            visitMulExp(addExp.getMulExp());
            if(thisValue!=null){
                tmpValue = Calculator.getCalculator().cal(thisValue,tmpValue,thisOperator);
            }
            if(addExp.getAddExp()!=null){
                tmpOperator = addExp.getOperator().token;
                visitAddExp(addExp.getAddExp());
            }
        }
        /*变量需要寄存器生成指令*/
        else {
            Reg lastReg = tmpReg;/*获取到同层横向之前计算结果的寄存器*/
            String thisOp = tmpOperator;/*获取到前面的计算符号*/
            tmpReg = null;/*下降前设置为null*/
            visitMulExp(addExp.getMulExp());
            if(lastReg != null){
                /*之前的结果如果是非空就生成指令，否则不操作*/
                Type thisUpType = tmpUpType;
                Reg thisReg = tmpReg;
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildBinary(curBlock,reg,thisOp,lastReg,thisReg,thisUpType);
                tmpReg = reg;/*把结果存入tmpReg传给下一级或者上一级*/
            }
            if(addExp.getOperator()!=null){
                tmpOperator = addExp.getOperator().token;/*获取到计算符号*/
                visitAddExp(addExp.getAddExp());
            }
        }
    }
    public void visitMulExp(MulExpNode mulExp){
        // MulExp -> UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        if(isConst){
            Integer thisValue = tmpValue;
            String thisOperator = tmpOperator;
            tmpValue = null;
            visitUnaryExp(mulExp.getUnaryExp());
            if(thisValue!=null){
                tmpValue = Calculator.getCalculator().cal(thisValue,tmpValue,thisOperator);
            }
            if(mulExp.getMulExp()!=null){
                tmpOperator = mulExp.getOperator().token;
                visitMulExp(mulExp.getMulExp());
            }
        }
        else {
            Reg lastReg = tmpReg;
            String thisOp = tmpOperator;
            tmpValue = null;
            visitUnaryExp(mulExp.getUnaryExp());
            if(lastReg!=null){
                Type thisUpType = tmpUpType;
                Reg thisReg = tmpReg;
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildBinary(curBlock,reg,thisOp,lastReg,thisReg,thisUpType);
                tmpReg = reg;
            }
            if(mulExp.getMulExp()!=null){
                tmpOperator = mulExp.getOperator().token;
                visitMulExp(mulExp.getMulExp());
            }
        }
    }
    public void visitUnaryExp(UnaryExpNode unaryExp){
        // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if(unaryExp.getPrimaryExp()!=null){/*第一种情况*/
            visitPrimaryExp(unaryExp.getPrimaryExp());/*直接向下访问即可*/
        }
        else if (unaryExp.getIdent()!=null) {/*第二种情况*/
            /*----------------递归下降终点之一（但参数还需要递归下降）----------------------*/
            /*由于函数调用可能会出现嵌套，所以operand是一个栈式列表*/
            String identName = unaryExp.getIdent().token;/*找到函数名称*/
            TableEntry tableEntry = curBlock.findSymbol(identName);/*查符号表*/
            boolean hasReturn = !(((FunctionType)tableEntry.type).getReturnType() == VarType._void);/*有返回值*/
            int curIdx = tmpOperand.size();/*当前的栈顶位置*/
            tmpOperand.add(tableEntry.reg);/*添加函数名称入栈*/
            if(unaryExp.getFuncRParams()!=null){
                /*下降到函数参数，需要将参数入栈*/
                visitFuncRParams(unaryExp.getFuncRParams());
            }
            List<Reg> thisOperand = new ArrayList<>(tmpOperand.subList(curIdx,tmpOperand.size()));/*获取当前函数操作数列表*/
            Call call;/*构造调用指令*/
            if(hasReturn){
                Reg reg = curBlock.regBuilder.build();/*调用结果寄存器*/
                call = new Call(reg,thisOperand,tableEntry.type);/*operandType传入FunctionType即可*/
                //System.out.print("Before finish:" + tmpOperand + " ");
                tmpOperand.subList(curIdx,tmpOperand.size()).clear();/*弹栈*/
                //System.out.println("After finish:" + tmpOperand);
                tmpReg = reg;/*综合运算结果*/
                tmpUpType = ((FunctionType)tableEntry.type).getReturnType();/*识别到的类型，由于这里是函数，需要改成返回值类型*/
            }else {
                call = new Call(thisOperand,tableEntry.type);
                tmpOperand.subList(curIdx,tmpOperand.size()).clear();/*弹栈*/
                /*无返回值不需要综合运算结果*/
            }
            curBlock.addInstruction(call);
        }
        else {/*第三种情况*/
            String thisOp = unaryExp.getUnaryOp().getOperator().token;
            if(thisOp.equals(LexType.PLUS.getValue())){
                //System.out.println("plus");
                /*对于+号，不需要做任何处理，交给下层处理即可(+unaryExp = unaryExp)*/
                visitUnaryExp(unaryExp.getUnaryExp());
            }
            else if (thisOp.equals(LexType.MINU.getValue())){
                //System.out.println("minus");
                /*对于-号，相当于与0做了一次减法*/
                visitUnaryExp(unaryExp.getUnaryExp());/*先通过下层处理获取到tmpReg和tmpValue*/
                if(isConst){
                    tmpValue = -tmpValue;/*常数计算结果传递过程中添加一个负号*/
                }else {
                    /*构造一条与0的减法指令，思路与前面AddExp和MulExp一样*/
                    Type thisUpType = tmpUpType;
                    Reg thisReg = tmpReg;/*下层传递上来的参数*/
                    Reg reg = curBlock.regBuilder.build();
                    irBuilder.buildBinary(curBlock,reg,thisOp,new Reg(0),thisReg,thisUpType);
                    tmpReg = reg;/*综合运算结果*/
                }
            }
            else {
                visitUnaryExp(unaryExp.getUnaryExp());
                /*对于!号，就相当于和0进行比较，!a相当于判断a==0*/
                Type thisUpType = tmpUpType;
                Reg thisReg = tmpReg;
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildBinary(curBlock,reg,"==",thisReg,new Reg(0),thisUpType);/*not仅一个运算寄存器，第二个为null*/
                tmpUpType = VarType.i1;/*运算结束之后类型就变成bool了*/
                tmpReg = reg;
            }
        }
    }
    public void visitFuncRParams(FuncRParamsNode funcRParams){
        // FuncRParams -> Exp { ',' Exp }
        for(ExpNode exp:funcRParams.getExps()){
            visitExp(exp);
            Reg thisReg = tmpReg;
            //System.out.println(tmpOperand);
            tmpOperand.add(thisReg);/*不需要判断isGlobal因为全局变量无法调用到函数，一定返回的是tmpReg*/
        }
    }
    public void visitPrimaryExp(PrimaryExpNode primaryExp){
        // PrimaryExp -> '(' Exp ')' | LVal | Number
        /*直接分类下发即可，类内不涉及到任何运算所以不需要任何指令*/
        if(primaryExp.getExp()!=null) {/*第一种情况*/
            visitExp(primaryExp.getExp());
        }
        else if(primaryExp.getLVal()!=null) {/*第二种情况*/
            visitLVal(primaryExp.getLVal());
        }
        else {/*第三种情况*/
            visitNumber(primaryExp.getNumber());
        }
    }
    public void visitNumber(NumberNode number){
        // Number -> IntConst
        //System.out.println(2);
        /*------------------------递归下降终点之一，判断是否是全局变量来获取到应该使用数值还是寄存器----------------------------*/
        if(isConst){
            tmpValue = Integer.parseInt(number.getIntConst().token);
        }else {
            tmpReg = new Reg(Integer.parseInt(number.getIntConst().token));
        }
        tmpUpType = VarType.i32;/*数字就是i32类型*/
    }
    public void visitLVal(LValNode lVal){
        // LVal -> Ident {'[' Exp ']'}
        /*------------------递归下降终点之一，在符号表内找到Ident之后获取对应tmpValue、tmpReg、tmpUpType---------------------*/
        String identName = lVal.getIdent().token;
        if(isConst){/*如果是全局变量，需要的是计算值而非任何寄存器*/
            if(lVal.getExps().isEmpty()){/*全局非数组变量*/
                /*需要通过查找符号表来获取到ident对应的值，出现在isGlobal部分说明一定是有初始值的，存在全局符号表内*/
                tmpValue = irModule.findSymbol(identName).initVal;/*综合属性接收点*/
                tmpUpType = irModule.findSymbol(identName).type;/*综合属性接受点*/
            }else {/*全局数组变量*/
                /*先根据Exp获取到坐标，然后取全局变量中对应位置的值出来作为value*/
                List<Integer> crd = new ArrayList<>();
                for(ExpNode exp:lVal.getExps()){
                    visitExp(exp);
                    Integer thisValue = tmpValue;
                    crd.add(thisValue);
                }
                TableEntry tableEntry = irModule.findSymbol(identName);
                Integer pos = ((ArrayType)tableEntry.type).getPosition(crd);
                tmpValue = tableEntry.initVals.get(pos);
                tmpUpType = ((ArrayType)tableEntry.type).getType();
            }
        }
        else {/*局部变量的左值表达式*/
            TableEntry tableEntry = curBlock.findSymbol(identName);
            /*这里需要特别注意，不能说Exp是empty就不是数组变量了，由于函数的特殊定义规则，参数可以是形如a的数组，只需要传入数组名即可*/
            if(lVal.getExps().isEmpty() && !(tableEntry.type instanceof ArrayType)){/*局部非数组变量*/
                /*查找符号表来获取到ident对应的值，直接生成一条load即可*/
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildLoad(curBlock,reg,tableEntry.reg, tableEntry.type);
                tmpReg = reg;
                tmpUpType = tableEntry.type;
            }
            else {/*局部数组变量*/
                /*根据Exp获取到坐标*/
                List<Reg> crd = new ArrayList<>();
                for(ExpNode exp:lVal.getExps()){
                    visitExp(exp);
                    Reg thisReg= tmpReg;
                    crd.add(thisReg);
                }
                /*分两种情况讨论，第一种是函数内调用形参数组，第二种是数组降维传参（例如3维数组改为2维数组作为值）*/
                Reg resReg = tableEntry.reg;/*最终处理完毕得到的结果寄存器*/
                Type resType = tableEntry.type;/*最终处理完毕得到的类型*/
                /*第一种情况：函数内调用了形参数组*/
                if(curFunction.symbolTable.findSymbol(identName)!=null){/*该函数在形参列表内*/
                    /*tableEntry中的type一定是pointer类型*/
                    /*TODO:这里有个问题就是我在所有指令内都没有使用指针而是直接在原指令上加了一个*，实际上应该使用指针，这里load出来本身就是指针类型，应该作为后面带*的参数，但是按照我的设计逻辑把这个类型传进去就变成了两个*，所以只能用去指针的类型做Type*/
                    /*先构造一条load指令*/
                    Reg loadReg = curBlock.regBuilder.build();
                    irBuilder.buildLoad(curBlock,loadReg,tableEntry.reg, tableEntry.type);
                    /*crd维度为空就跳过*/
                    if(crd.isEmpty()) {
                        resType = tableEntry.type;
                        resReg = loadReg;
                    }
                    /*crd维度不为空，先构造一条offset寻址，把数组指针去为数组*/
                    else {
                        Reg gepReg = curBlock.regBuilder.build();
                        resType = ((PointerType)tableEntry.type).getTarget();/*一定是指向数组的指针*/
                        irBuilder.buildGEP(curBlock,crd.get(0),gepReg,loadReg,resType);
                        crd = new ArrayList<>(crd.subList(1,crd.size()));
                        resReg = gepReg;
                    }
                }
                /*到这里resType一定是数组类型*/
                if(!crd.isEmpty()){/*降维*/
                    int level = crd.size();/*下降的维度*/
                    Reg gepReg = curBlock.regBuilder.build();
                    irBuilder.buildGEP(curBlock,gepReg,resReg,crd,resType);
                    resType = ((ArrayType)resType).array2array(level);
                    resReg = gepReg;
                }
                /*判断结果类型如果是i32也就是说resType中的dimension=0，就进行load，否则化为指针*/
                if(((ArrayType)resType).dimension()==0){
                    resType = ((ArrayType) resType).getType();
                    Reg loadReg = curBlock.regBuilder.build();
                    irBuilder.buildLoad(curBlock,loadReg,resReg,resType);
                    resReg = loadReg;
                }else {
                    Reg gepReg = curBlock.regBuilder.build();
                    List<Reg> zero = new ArrayList<>();
                    zero.add(new Reg(0));
                    irBuilder.buildGEP(curBlock,gepReg,resReg,zero,resType);
                    resType = ((ArrayType)resType).array2pointer();
                    resReg = gepReg;
                }
                tmpUpType = resType;
                tmpReg = resReg;
            }
        }
    }



    public void visitCond(CondNode cond){
        // Cond -> LOrExp
        visitLOrExp(cond.getLOrExp());
    }
    public void visitLOrExp(LOrExpNode lOrExp){
        // LOrExp -> LAndExp | LAndExp '||' LOrExp
        BasicBlock thisBlock = curBlock;
        /*获取继承属性*/
        BasicBlock thisTrue = tmpTrue;/*保存if语句的trueBlock地址*/
        BasicBlock thisFalse = tmpFalse;/*保存if语句的falseBlock地址*/
        BasicBlock thisNext;
        /*判断后面是否还有LOr节点，如果有就把tmpFalse设置成它的地址，否则tmpFalse直接设置成if语句的falseBlock地址*/
        if(lOrExp.getLOrExp()!=null){
            thisNext = irBuilder.buildCondBlock(curFunction,curBlock);
        }
        else thisNext = thisFalse;
        tmpTrue = thisTrue;/*更新继承属性：if的trueBlock地址*/
        tmpFalse = thisNext;/*更新继承属性：下一个LOr的地址或者if的falseBlock地址*/
        visitLAndExp(lOrExp.getLAndExp());
        /*把继承属性设置回if语句的true和falseBlock属性，传递给下一个LOr节点*/
        tmpTrue = thisTrue;
        tmpFalse = thisFalse;
        if(lOrExp.getLOrExp()!=null){
            curBlock = thisNext;/*进入下一个LOr节点*/
            visitLOrExp(lOrExp.getLOrExp());
            thisBlock.addInstructionList(thisNext.getInstructions());
        }
    }
    public void visitLAndExp(LAndExpNode lAndExp){
        // LAndExp -> EqExp | EqExp '&&' LAndExp
        BasicBlock thisBlock = curBlock;
        /*获取继承属性*/
        BasicBlock thisTrue = tmpTrue;/*保存if语句的trueBlock地址*/
        BasicBlock thisFalse = tmpFalse;/*保存下一个LOr的地址或者if的falseBlock地址*/
        BasicBlock thisNext;
        /*判断后面是否还有LAnd节点，如果有就把tmpTrue设置成它的地址，否则直接设置成if语句的trueBlock地址*/
        if(lAndExp.getLAndExp()!=null){
            thisNext = irBuilder.buildCondBlock(curFunction,curBlock);
        }
        else thisNext = thisTrue;
        /*下降获取计算eqExp计算结果*/
        tmpReg = null;
        visitEqExp(lAndExp.getEqExp());
        /*构造指令*/
        Reg thisReg = tmpReg;/*获取到eqExp的结果，eqExp中应该是tmpReg=icmp eq/ne %i,%j的形式，tmpReg是i32类型*/
        Type thisUpType = tmpUpType;
        /*只有独立表达式不会参与比较，也就是只有一个EqExp->RelExp->AddExp节点，这时候综合属性tmpReg的类型还是i32*/
        Reg zextReg = thisReg;/*直接用tmpReg(i32)进行比较*/
        if(thisUpType==VarType.i1){
            /*如果在EqExp和RelExp中参与了比较，那么结果一定是i1类型，需要转化为i32再进行icmp*/
            zextReg = curBlock.regBuilder.build();
            irBuilder.buildZext(curBlock,zextReg,thisReg,thisUpType,VarType.i32);/*Zext指令把eqExp的结果转为i32类型*/
            thisUpType = VarType.i32;
            /*这里不需要更新tmpUpType因为LAnd已经属于顶层了，上面的cond中用不到tmpUpType*/
        }
        Reg binaryReg = curBlock.regBuilder.build();
        irBuilder.buildBinary(curBlock,binaryReg,"!=",new Reg(0),zextReg,thisUpType);/*比较eqExp的结果是否是0*/
        irBuilder.buildBr(curBlock,binaryReg,thisNext.getLabel(),thisFalse.getLabel());
        /*设置好继承属性*/
        tmpTrue = thisTrue;
        tmpFalse = thisFalse;
        if(lAndExp.getLAndExp()!=null){
            curBlock = thisNext;/*进入下一个LAnd节点*/
            visitLAndExp(lAndExp.getLAndExp());
            thisBlock.addInstructionList(thisNext.getInstructions());/*把后面的指令全部添加进来*/
        }
    }
    public void visitEqExp(EqExpNode eqExp){
        // EqExp -> RelExp | RelExp ('==' | '!=') EqExp
        Reg lastReg = tmpReg;
        String thisOp = tmpOperator;
        Type lastUpType = tmpUpType;/*上一个RelExp的类型*/
        tmpReg = null;
        visitRelExp(eqExp.getRelExp());
        Reg thisReg = tmpReg;
        Type thisUpType = tmpUpType;/*上一个RelExp的类型*/
        if(thisUpType==VarType.i1){/*比较的icmp指令是32位的，在RelExp的结果有可能是i1，需要转化为i32*/
            Reg reg = curBlock.regBuilder.build();
            irBuilder.buildZext(curBlock,reg,thisReg,thisUpType,VarType.i32);
            thisUpType =  VarType.i32;
            thisReg = reg;
            tmpReg = reg;
            tmpUpType = VarType.i32;
        }
        if(lastReg!=null){
            if (lastUpType == VarType.i1) {/*前面的一系列RelExp计算结果如果是i1类型的，则需要转化成i32类型的*/
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildZext(curBlock,reg,lastReg,lastUpType,thisUpType);
                lastReg = reg;
            }
            Reg reg = curBlock.regBuilder.build();
            irBuilder.buildBinary(curBlock,reg,thisOp,lastReg,thisReg,thisUpType);
            tmpReg = reg;
            tmpUpType = VarType.i1;/*经过icmp比较之后的类型变为i1*/
        }
        if(eqExp.getEqExp()!=null){
            tmpOperator = eqExp.getOperator().token;
            visitEqExp(eqExp.getEqExp());
        }
    }
    public void visitRelExp(RelExpNode relExp){
        // RelExp -> AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
        Reg lastReg = tmpReg;
        String thisOp = tmpOperator;
        Type lastUpType = tmpUpType;/*前面的RelExp的类型*/
        tmpReg = null;
        visitAddExp(relExp.getAddExp());
        Reg thisReg = tmpReg;
        Type thisUpType = tmpUpType;
        if(thisUpType==VarType.i1){/*比较的icmp指令是32位的，如果下面在unaryExp里出现了!号则需要转为32位*/
            Reg reg = curBlock.regBuilder.build();
            irBuilder.buildZext(curBlock,reg,thisReg,thisUpType,VarType.i32);
            thisReg = reg;
            thisUpType = VarType.i32;
            tmpReg = reg;
            tmpUpType = VarType.i32;
        }
        if(lastReg!=null){
            if (lastUpType == VarType.i1) {/*前面的一系列AddExp计算结果如果是i1类型的，则需要转化成i32类型的*/
                Reg reg = curBlock.regBuilder.build();
                irBuilder.buildZext(curBlock,reg,lastReg,lastUpType,thisUpType);
                lastReg = reg;
            }
            Reg reg = curBlock.regBuilder.build();
            irBuilder.buildBinary(curBlock,reg,thisOp,lastReg,thisReg,thisUpType);
            tmpReg = reg;
            tmpUpType = VarType.i1;/*经过icmp指令比较之后的类型变成i1*/
        }

        if(relExp.getRelExp()!=null){
            tmpOperator = relExp.getOperator().token;
            visitRelExp(relExp.getRelExp());
        }
    }
}
