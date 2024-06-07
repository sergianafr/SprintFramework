package src.exception;

public class MyException {
    String cause;
    public MyException(String cause){
        this.cause = cause;
    }
    public MyException(){}
}
