package src.mg.itu.prom16.exceptions;

import jakarta.servlet.ServletException;

public class ReturnTypeException extends ServletException {
    public ReturnTypeException(String message){
        super(message);
    }
}
