package src.mg.itu.prom16.exceptions;

import javax.servlet.ServletException;

public class ReturnTypeException extends ServletException {
    public ReturnTypeException(String message){
        super(message);
    }
}
