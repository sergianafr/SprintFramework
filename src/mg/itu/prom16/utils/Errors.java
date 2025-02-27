package src.mg.itu.prom16.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Errors {
    private HashMap<String, List<String>> fieldMessage = new HashMap<String, List<String>>();
    private String redirectionUrl;

    public String getRedirectionUrl() {
        return redirectionUrl;
    }
    
    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }
    public void addError(String field, String message) {
        if(fieldMessage.containsKey(field)) {
            fieldMessage.get(field).add(message);
        } else {
            List<String> messages = new ArrayList<String>();
            fieldMessage.put(field, messages);
            fieldMessage.get(field).add(message);
        }
        // fieldMessage.put(field, message);
    }
    
    public boolean isEmpty() {
        return fieldMessage.isEmpty();
    }
}
