package controller;

public class Calculator {
    private static final Calculator calculator = new Calculator();
    /*构造函数*/
    private Calculator(){}

    public static Calculator getCalculator(){return calculator;}

    public Integer cal(Integer op1, Integer op2, String op){
        switch (op) {
            case "+" -> {
                return op1 + op2;
            }
            case "-" -> {
                return op1 - op2;
            }
            case "*" -> {
                return op1 * op2;
            }
            case "/" -> {
                return op1 / op2;
            }
            case "%" -> {
                return op1 % op2;
            }
            default -> {
                return null;
            }
        }
    }
}
