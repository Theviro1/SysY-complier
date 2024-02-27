package dao;

public enum LexType {
    IDENFR("Ident","Ident"),
    INTCON("IntConst","IntConst"),
    STRCON("FormatString","FormatString"),
    MAINTK("main","keyword"),
    CONSTTK("const","keyword"),
    INTTK("int","keyword"),
    BREAKTK("break","keyword"),
    CONTINUETK("continue","keyword"),
    IFTK("if","keyword"),
    ELSETK("else","keyword"),
    NOT("!","keyword"),
    AND("&&","keyword"),
    OR("||","keyword"),
    FORTK("for","keyword"),
    GETINTTK("getint","keyword"),
    PRINTFTK("printf","keyword"),
    RETURNTK("return","keyword"),
    PLUS("+","sign"),
    MINU("-","sign"),
    VOIDTK("void","sign"),
    MULT("*","sign"),
    DIV("/","sign"),
    MOD("%","sign"),
    LSS("<","sign"),
    LEQ("<=","sign"),
    GRE(">","sign"),
    GEQ(">=","sign"),
    EQL("==","sign"),
    NEQ("!=","sign"),
    ASSIGN("=","sign"),
    SEMICN(";","sign"),
    COMMA(",","sign"),
    LPARENT("(","sign"),
    RPARENT(")","sign"),
    LBRACK("[","sign"),
    RBRACK("]","sign"),
    LBRACE("{","sign"),
    RBRACE("}","sign");
    private final String value;/*单词名称：所有的程序内的字符*/
    private final String type;/*单词类别：包括了关键字(keyword)、符号(sign)以及标识符数字和常量*/
    LexType(String value,String type){
        this.value=value;
        this.type=type;
    }
    public String getValue(){return this.value;}
    public String getType(){return this.type;}
    //给出字符串的值，根据值找到对应的类别码
    public static String findCode(String value,String type){
        /*先判断种类：数字或者字符串、标识符常量单独判断*/
        if(type.equals("Ident")){/*自定义标识符*/
            return IDENFR.name();
        }
        if(type.equals("IntConst")){/*数字常量*/
            return INTCON.name();
        }
        if(type.equals("FormatString")) {/*字符串*/
            return STRCON.name();
        }
        /*再根据输入的查找*/
        for(LexType lexType:LexType.values()){
            if(lexType.value.equals(value)){
                return lexType.name();
            }
        }
        return null;/*没有找到就返回null*/
    }
}
