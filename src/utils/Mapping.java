package src.utils;

import java.lang.reflect.Method;

public class Mapping {
    String className;
    String methodName;
    public Mapping(String key, String value) {
        this.className = key;
        this.methodName = value;
    }
    
    public Object invoke() throws Exception {
        Object o = null;
        
        Class<?> clazz = Class.forName(this.className);
        Object ins = clazz.getConstructor().newInstance();

        Method m = clazz.getMethod(this.methodName, null);

        o = m.invoke(ins, null);

        return o;
    }


}
 