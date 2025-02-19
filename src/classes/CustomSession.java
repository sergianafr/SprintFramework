package src.classes;

import java.util.HashMap;

import jakarta.servlet.http.HttpSession;

public class CustomSession {
    private HashMap<String, Object> values;
    private HttpSession session;

    public CustomSession(){
        this.values = new HashMap<String, Object>();
    }

    public void add(String key, Object value) {
        this.values.put(key, value);
        this.session.setAttribute(key, value);
    }
    public void remove(String key) {
        this.values.remove(key);
        this.session.removeAttribute(key);
    }
    public Object get(String key) {
        // return this.values.get(key);
        return this.session.getAttribute(key);
    
    }
    

    public void castToHttpSession(HttpSession session) throws Exception{
        src.utils.Utils.emptySession(session);
        for (String key : this.values.keySet()) {
            session.setAttribute(key, this.values.get(key));
        }
    }


    public CustomSession(HttpSession session) {
        this.values = new HashMap<String, Object>();
        this.session = session;
    }

    public static CustomSession castToCustomSession(HttpSession session){
        HashMap<String, Object> values = src.utils.Utils.getSessionValues(session);
        CustomSession cs = new CustomSession();
        for (String key : values.keySet()) {
            cs.add(key, values.get(key));
        }
        return cs;
    }

}
