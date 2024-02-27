package service;

import dao.LexType;
import dao.Token;

public class Lexer {
    private String source;/*源程序代码*/
    private Integer curPos=0;/*当前指针位置*/
    private String token;/*解析单词值*/
    private String type;/*解析单词类型*/
    private Integer lineNum;/*当前行号*/
    private static final Lexer lexer=new Lexer();/*私有单例*/
    //构造函数
    private Lexer(){}
    //getter & setter
    public Token getToken(){
        return new Token(this.token, this.type);
    }
    public Integer getLineNum(){return this.lineNum;}
    public void setLineNum(Integer lineNum){this.lineNum = lineNum;}
    public static Lexer getLexer(){return lexer;}/*获取单例*/
    //自定义函数
    /*setUp：刷新编译器状态*/
    public void setUp(String source) {
        this.token="";
        this.type="";
        this.lineNum=1;
        this.curPos=0;
        this.source = source;/*设定源代码*/
    }

    //next：处理下一个单词，当处理到结尾时返回1，错误时返回-1，否则返回0
    public boolean next(){
        /*理论上每次curPos++时都要判断一次是否到达代码结尾，但是这里先预处理给source后添加'\0'字符*/
        token="";/*清空token*/
        type="";/*清空type*/
        /*判断并跳过所有空白字符*/
        while(source.charAt(curPos)==' '||
              source.charAt(curPos)=='\t'||
              source.charAt(curPos)=='\r'||
              source.charAt(curPos)=='\n'||
              source.charAt(curPos)=='\0'
        ){
            if(source.charAt(curPos)=='\0') return false;
            if(source.charAt(curPos)=='\n') lineNum++;
            curPos++;
        }
        /*判断是否是数字常量*/
        if(Character.isDigit(source.charAt(curPos))){
            type= LexType.findCode(token,"IntConst");
            while(Character.isDigit(source.charAt(curPos))){
                token+=source.charAt(curPos);
                curPos++;
            }
        }
        /*判断是否是标识符或者关键字*/
        else if(Character.isLetter(source.charAt(curPos))||source.charAt(curPos)=='_'){
            while(Character.isLetter(source.charAt(curPos)) ||
                  Character.isDigit(source.charAt(curPos)) ||
                  source.charAt(curPos)=='_'
            ){
                token+=source.charAt(curPos);
                curPos++;
            }
            /*判断是否是关键字，否则就是标识符*/
            String lexType=LexType.findCode(token,"keyword");
            if(lexType!=null) type=lexType;
            else type=LexType.findCode(token,"Ident");
        }
        /*判断是否是字符串常量*/
        else if(source.charAt(curPos)=='"'){
            curPos++;
            token+='\"';
            type=LexType.findCode(token,"FormatString");
            while(source.charAt(curPos)!='"'){
                token+=source.charAt(curPos);
                curPos++;
            }
            curPos++;
            token+='\"';
        }
        /*判断是否是字符常量*/
        else if(source.charAt(curPos)=='\''){
            curPos++;
            token+='\'';
            type=LexType.findCode(token,"FormatString");
            while(source.charAt(curPos)!='\''){
                token+=source.charAt(curPos);
                curPos++;
            }
            curPos++;
            token+='\'';
        }
        /*判断是否是字符*/
        else{
            token+=source.charAt(curPos);
            /*一般字符只有一位长度，先判断多位长度的字符*/
            /*>=*/
            if(source.charAt(curPos)=='>'){
                curPos++;/*下一位是否是=号*/
                if(source.charAt(curPos)=='='){/*是则添加到token内*/
                    token+=source.charAt(curPos);
                }
                else curPos--;/*不是则回退指针*/
            }
            /*<=*/
            else if(source.charAt(curPos)=='<'){
                curPos++;/*下一位是否是=号*/
                if(source.charAt(curPos)=='='){/*是则添加到token内*/
                    token+=source.charAt(curPos);
                }
                else curPos--;/*不是则回退指针*/
            }
            /*==*/
            else if(source.charAt(curPos)=='='){
                curPos++;/*下一位是否是=号*/
                if(source.charAt(curPos)=='='){/*是则添加到token内*/
                    token+=source.charAt(curPos);
                }
                else curPos--;/*不是则回退指针*/
            }
            /*!=*/
            else if(source.charAt(curPos)=='!'){
                curPos++;/*下一位是否是=号*/
                if(source.charAt(curPos)=='='){/*是则添加到token内*/
                    token+=source.charAt(curPos);
                }
                else curPos--;/*不是则回退指针*/
            }
            /*||*/
            else if(source.charAt(curPos)=='|'){
                curPos++;/*下一位是否是=号*/
                if(source.charAt(curPos)=='|'){/*是则添加到token内*/
                    token+=source.charAt(curPos);
                }
                else curPos--;/*不是则回退指针*/
            }
            /*&&*/
            else if(source.charAt(curPos)=='&'){
                curPos++;/*下一位是否是=号*/
                if(source.charAt(curPos)=='&'){/*是则添加到token内*/
                    token+=source.charAt(curPos);
                }
                else curPos--;/*不是则回退指针*/
            }

            curPos++;
            String lexType=LexType.findCode(token,"sign");
            if(lexType==null) System.out.println("error!");/*找不到说明不合法*/
            else type=lexType;
        }
        return true;
    }
    //preRead：超前读取下一个字符，但是不影响当前指针位置
    public Token preRead(int offset){
        /*preRead(0)就等于next*/
        Integer pos = curPos;
        Integer line = lineNum;
        while(offset>=0 && lexer.next()){
            offset--;
        }
        Token result = new Token(token, type);
        curPos = pos;
        lineNum = line;
        return result;
    }

    //setCurPos：设定当前指针的位置
    public void setCurPos(Integer curPos){
        this.curPos=curPos;
    }
    //getCurPos：获取当前指针的位置
    public Integer getCurPos(){
        return this.curPos;
    }
}
