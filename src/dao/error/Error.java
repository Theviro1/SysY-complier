package dao.error;

public class Error {
    private Integer lineNum;
    private ErrorType errorType;
    public Error(Integer lineNum, ErrorType errorType){
        this.lineNum = lineNum;
        this.errorType = errorType;
    }
    @Override
    public String toString(){
        return lineNum + " " + errorType.toString() + "\n";
    }
}
