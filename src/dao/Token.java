package dao;

public class Token {
    public String token;
    public String type;

    public Token(){}
    public Token(String token,String type){
        this.token=token;
        this.type=type;
    }

    @Override
    public String toString() {
        if(type!=null) return type+" "+token+"\n";
        else return token+"\n";
    }
}
