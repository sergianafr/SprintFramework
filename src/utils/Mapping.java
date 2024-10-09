package src.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import src.annotations.Param;
import src.classes.CustomSession;

public class Mapping {
    String className;
    String methodName;
    Parameter[] parameters;

    public Mapping(String key, String value, Parameter[] parameters) {
        this.className = key;
        this.methodName = value;
        this.parameters = parameters;
    }
    
    public Class<?>[] getTypes(){
        Class<?>[] clazz = new Class[this.parameters.length];
        int i = 0;
        for (Parameter param : this.parameters) {
            clazz[i] = param.getType();
            i++;
        }
        return clazz;
    }
    public Object invoke() throws Exception {
        Object o = null;
        
        Class<?> clazz = Class.forName(this.className);
        Object ins = clazz.getConstructor().newInstance();

        Method m = clazz.getMethod(this.methodName, null);

        o = m.invoke(ins, null);

        return o; 
    }
    public Object invoke(Object[] parameters) throws Exception {
        Object o = null;
        
        Class<?> clazz = Class.forName(this.className);
        Object ins = clazz.getConstructor().newInstance();
        Class<?>[] types = null;
        if (parameters != null) {
            types = this.getTypes();
        }
        Method m = clazz.getMethod(this.methodName, types);
        o = m.invoke(ins, parameters);

        return o;
    }
    public Class<?> getReturnType() throws Exception {
        Class<?> clazz = null;

        try {
            Method m = Class.forName(this.getClassName()).getDeclaredMethod(this.getMethodName(), this.getTypes());
            clazz = m.getReturnType();
        } catch (Exception e) {
            throw e;
        } 

        return clazz;
    }

    public Object invoke(String[] parameters) throws Exception {
        Object o = null;
        
        Class<?> clazz = Class.forName(this.className);
        Object ins = clazz.getConstructor().newInstance();
        Class<?>[] types = null;
        if (parameters != null) {
            types = this.getTypes();
        }
        Method m = clazz.getMethod(this.methodName, types);
        o = m.invoke(ins, parameters);

        return o;
    }

    public void checkParam() throws Exception {
        for(Parameter p : this.parameters){
            if(!p.isAnnotationPresent(Param.class) && p.getType() != CustomSession.class){
                throw new Exception("ETU 002610 param not annotated");
            }
        }
    }
    public String getClassName() {
        return className;
    }
    public String getMethodName() {
        return methodName;
    }
    public Parameter[] getParameters() {
        return parameters;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }
    // public void setParameters(Parameter[] parameters) throws Exception {
    //     if(Utils.checkParameters(parameters)){
    //         this.parameters = parameters;
    //     } else{
    //         throw new IllegalArgumentException("ETU002610 // Les parametres de la methode doivent etre annotés");
    //     }
    // }


}
 