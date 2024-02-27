package service;

import dao.Token;
import dao.error.Error;
import dao.nodes.CompUnitNode;
import service.Generator;
import service.Lexer;
import service.Parser;

import java.io.*;

public class CompilerService {
    private final Lexer lexer;
    private final Parser parser;
    private final Generator generator;


    public CompilerService() {
        this.generator=Generator.getGenerator();
        this.lexer=Lexer.getLexer();
        this.parser=Parser.getParser();
    }
    /*删除所有注释*/
    public String uncomment(String sourceCode){
        StringBuilder sb = new StringBuilder(sourceCode);
        for (int i = 0; i < sourceCode.length(); ) {
            if (sourceCode.charAt(i) == '"') {
                do {
                    i += 1;
                } while (sourceCode.charAt(i) != '"');
                i += 1;
            } else if (sourceCode.charAt(i) == '/' &&
                    i + 1 < sourceCode.length() &&
                    sourceCode.charAt(i + 1) == '/') {
                sb.replace(i, i + 2, "  ");
                i += 2;
                while (sourceCode.charAt(i) != '\n') {
                    sb.replace(i, i + 1, " ");
                    i += 1;
                }
                i += 1;
            } else if (sourceCode.charAt(i) == '/' &&
                    i + 1 < sourceCode.length() &&
                    sourceCode.charAt(i + 1) == '*') {
                sb.replace(i, i + 2, "  ");
                i += 2;
                while (sourceCode.charAt(i) != '*' || sourceCode.charAt(i + 1) != '/') {
                    sb.replace(i, i + 1, sourceCode.charAt(i) == '\n' ? "\n" : " ");
                    i += 1;
                }
                sb.replace(i, i + 2, "  ");
                i += 2;
            } else {
                i += 1;
            }
        }
        return sb.toString();
    }
    /*执行编译过程*/
    public void serve() throws Exception{
        /*文件输入*/
        StringBuilder sourceBuilder=new StringBuilder();
        File testfile=new File("testfile.txt");
        if(testfile.createNewFile()){
            System.out.println("create new file success");
        }else System.out.println("file already exist");
        FileReader fileReader=new FileReader(testfile);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        String line;
        while((line=bufferedReader.readLine())!=null){
            sourceBuilder.append(line);
            sourceBuilder.append('\n');
        }
        bufferedReader.close();
        fileReader.close();
        String source=sourceBuilder.toString();

        /*预处理源代码，要求删去注释，并在结尾添加\0字符*/
        source=uncomment(source);
        System.out.println(source);
        source+='\0';
        lexer.setUp(source);


        /*文件输出*/
        //error.txt
        File output1=new File("error.txt");
        if(output1.createNewFile()){
            System.out.println("create new file success");
        }else System.out.println("file already exist");
        FileWriter fileWriter1=new FileWriter(output1);
        BufferedWriter bufferedWriter1=new BufferedWriter(fileWriter1);
        //llvm_ir.txt
        File output2=new File("llvm_ir.txt");
        if(output2.createNewFile()){
            System.out.println("create new file success");
        }else System.out.println("file already exist");
        FileWriter fileWriter2=new FileWriter(output2);
        BufferedWriter bufferedWriter2=new BufferedWriter(fileWriter2);

        /*执行编译过程*/
        String res1="";
        String res2;
        //语法分析
        CompUnitNode root = parser.CompUnit();
        if (!parser.getErrorList().isEmpty()){
            for(Error error: parser.getErrorList()){
                res1 += error.toString();
            }
            bufferedWriter1.write(res1);
        }
        //代码生成
        else {
            generator.visitCompUnit(root);
            res2 = generator.getIrModule().toString();
            bufferedWriter2.write(res2);
        }

        bufferedWriter2.close();
        fileWriter2.close();
        bufferedWriter1.close();
        fileWriter1.close();
    }


}
