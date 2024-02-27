import service.CompilerService;

public class Compiler {
    public static void main(String[] args) {
        CompilerService compilerService=new CompilerService();
        try {
            compilerService.serve();
        }catch (Exception ignored){}
    }
}
