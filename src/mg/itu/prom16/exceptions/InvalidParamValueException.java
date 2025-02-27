package src.mg.itu.prom16.exceptions;

import src.mg.itu.prom16.utils.Errors;

public class InvalidParamValueException extends Exception {
    private Errors errors = new Errors();
    
    public InvalidParamValueException(String message){
        super("Parameter value is invalid: "+message);
    }
    public InvalidParamValueException(Errors errors){
        super("There are issues with the parameters, please check the errors to solve them.");
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }
    public void setErrors(Errors errors) {
        this.errors = errors;
    }

}
