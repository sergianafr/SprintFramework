package src.mg.itu.prom16.utils;
import src.mg.itu.prom16.enumeration.*;

import java.lang.reflect.Method;
import java.util.HashMap;

public class VerbMethod extends HashMap<Verbs, Method>{

    public VerbMethod(Verbs verb, Method method) {
        super();
        this.put(verb, method);
    }
}
