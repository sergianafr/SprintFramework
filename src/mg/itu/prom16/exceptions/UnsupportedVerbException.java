package src.mg.itu.prom16.exceptions;

import jakarta.servlet.ServletException;

public class UnsupportedVerbException extends ServletException {
    public UnsupportedVerbException(String verb) {
        super("The verb requested is not supported by the method: " + verb);
    }
    

}
