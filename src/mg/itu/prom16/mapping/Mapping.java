package src.mg.itu.prom16.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;
import jakarta.servlet.http.HttpServletRequest;

import src.mg.itu.prom16.annotations.Param;
import src.mg.itu.prom16.classes.CustomSession;
import src.mg.itu.prom16.enumeration.Verbs;
import src.mg.itu.prom16.utils.Utils;
import src.mg.itu.prom16.utils.VerbMethod;

public class Mapping {
    Class<?> controllerClass;
    VerbMethod verbMethod;
    public VerbMethod getVerbMethod() {
        return verbMethod;
    }
    public void setVerbMethod(VerbMethod verbMethod) {
        this.verbMethod = verbMethod;
    }
    public void setVerbMethod(Verbs verb, Method method) {
        this.verbMethod = new VerbMethod(verb, method);
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }
    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }
    // Parameter[] parameters;

    public Mapping(Class<?> key, VerbMethod verbMethod) {
        this.controllerClass = key;
        this.verbMethod = verbMethod;
        // this.parameters = parameters;
    }

    public Method getMethod(Verbs verb)throws Exception {
        if(verbMethod.get(verb) != null){
            return verbMethod.get(verb);
        }
        throw new Exception("No method found for this verb");
        
    }


    public Class<?> getReturnType(Verbs verb) throws Exception {
        Class<?> clazz = null;

        try {
            Method m = verbMethod.get(verb);
            clazz = m.getReturnType();
        } catch (Exception e) {
            throw e;
        } 

        return clazz;
    }
    

    // Ensuring that all parameters are annotated 
    public void checkParam(Verbs requestVerb)throws Exception {
        Method m = verbMethod.get(requestVerb);
        Parameter[] parameters = m.getParameters();
        for(Parameter p : parameters){
            if(!p.isAnnotationPresent(Param.class) && p.getType() != CustomSession.class){
                throw new InvalidAttributesException("ETU 002610 param not annotated");
            }
        }
    }
    
    // Getting the values of the method parameters 
    public Object[] findParams(HttpServletRequest req, Verbs requestVerb) throws Exception {
        List<Object> args = new ArrayList<>();
        Method method = verbMethod.get(requestVerb);
    
        try {
            for (Parameter parameter : method.getParameters()) {
                Object arg = processParameter(req, parameter);
                args.add(arg);
            }
        } catch (Exception e) {
            throw e;
        }
    
        return args.isEmpty() ? null : args.toArray();
    }
    
    private Object processParameter(HttpServletRequest req, Parameter parameter) throws Exception {
        String key = getParameterKey(parameter);
        Class<?> paramType = parameter.getType();
    
        if (paramType == CustomSession.class) {
            return createCustomSession(req);
        } else if (isObject(paramType)) {
            return createObject(req, key, paramType);
        } else {
            return createSimpleObject(req, key, paramType);
        }
    }
    
    private String getParameterKey(Parameter parameter) {
        if (parameter.isAnnotationPresent(Param.class)) {
            Param annotationParam = parameter.getAnnotation(Param.class);
            return annotationParam.name();
        }
        return "";
    }
    
    // checking if the class is an object and not a primitive ou session
    private boolean isObject(Class<?> paramType) {
        return !paramType.isPrimitive() && paramType != String.class && paramType != CustomSession.class;
    }
    
    private CustomSession createCustomSession(HttpServletRequest req) {
        return new CustomSession(req.getSession());
    }
    
    private Object createObject(HttpServletRequest req, String key, Class<?> paramType) throws Exception {
        Object obj = paramType.getDeclaredConstructor().newInstance();
        Field[] fields = paramType.getDeclaredFields();
    
        for (Field field : fields) {
            String fieldKey = buildFieldKey(key, field);
            String fieldValue = req.getParameter(fieldKey);
    
            if (fieldValue != null) {
                setFieldValue(obj, field, fieldValue);
            }
        }
    
        return obj;
    }
    
    private String buildFieldKey(String baseKey, Field field) {
        String fieldKey = baseKey + ".";
        if (field.isAnnotationPresent(src.mg.itu.prom16.annotations.Field.class)) {
            src.mg.itu.prom16.annotations.Field annotation = field.getAnnotation(src.mg.itu.prom16.annotations.Field.class);
            fieldKey += annotation.name();
        } else {
            fieldKey += field.getName();
        }
        return fieldKey;
    }
    
    private void setFieldValue(Object obj, Field field, String fieldValue) throws Exception {
        Method setter = Utils.setter(field, obj.getClass());
        Object convertedValue = Utils.convert(fieldValue, field.getType());
        setter.invoke(obj, convertedValue);
    }
    
    private Object createSimpleObject(HttpServletRequest req, String key, Class<?> paramType) throws Exception {
        String valueStr = req.getParameter(key);
        return Utils.convert(valueStr, paramType);
    }

    public Object invoke(Verbs requestVerb, HttpServletRequest request)throws Exception{
        Object o = null;
        try {
            checkParam(requestVerb);
            Method m = verbMethod.get(requestVerb);
            o = m.invoke(controllerClass.getConstructor().newInstance(), findParams((HttpServletRequest) request, requestVerb));
        } catch (Exception e) {
            throw e;
        }
        return o;
    }



    // public Class<?>[] getTypes(){
    //     Class<?>[] clazz = new Class[this.parameters.length];
    //     int i = 0;
    //     for (Parameter param : this.parameters) {
    //         clazz[i] = param.getType();
    //         i++;
    //     }
    //     return clazz;
    // }
    // public Object invoke() throws Exception {
    //     Object o = null;
        
    //     Class<?> clazz = Class.forName(this.className);
    //     Object ins = clazz.getConstructor().newInstance();

    //     Method m = clazz.getMethod(this.methodName, null);

    //     o = m.invoke(ins, null);

    //     return o; 
    // }
    // public Object invoke(Object[] parameters) throws Exception {
    //     Object o = null;
        
    //     Class<?> clazz = Class.forName(this.className);
    //     Object ins = clazz.getConstructor().newInstance();
    //     Class<?>[] types = null;
    //     if (parameters != null) {
    //         types = this.getTypes();
    //     }
    //     Method m = clazz.getMethod(this.methodName, types);
    //     o = m.invoke(ins, parameters);

    //     return o;
    // }
    // public Class<?> getReturnType() throws Exception {
    //     Class<?> clazz = null;

    //     try {
    //         Method m = Class.forName(this.getClassName()).getDeclaredMethod(this.getMethodName(), this.getTypes());
    //         clazz = m.getReturnType();
    //     } catch (Exception e) {
    //         throw e;
    //     } 

    //     return clazz;
    // }

    // public Object invoke(String[] parameters) throws Exception {
    //     Object o = null;
        
    //     Class<?> clazz = Class.forName(this.className);
    //     Object ins = clazz.getConstructor().newInstance();
    //     Class<?>[] types = null;
    //     if (parameters != null) {
    //         types = this.getTypes();
    //     }
    //     Method m = clazz.getMethod(this.methodName, types);
    //     o = m.invoke(ins, parameters);

    //     return o;
    // }

    // public void checkParam() throws Exception {
    //     for(Parameter p : this.parameters){
    //         if(!p.isAnnotationPresent(Param.class) && p.getType() != CustomSession.class){
    //             throw new Exception("ETU 002610 param not annotated");
    //         }
    //     }
    // }
    
    // public String getClassName() {
    //     return className;
    // }
    // public String getMethodName() {
    //     return methodName;
    // }
    // public Parameter[] getParameters() {
    //     return parameters;
    // }
    // public void setClassName(String className) {
    //     this.className = className;
    // }
    // public void setMethodName(String methodName) {
    //     this.methodName = methodName;
    // }
    // public void setParameters(Parameter[] parameters) {
    //     this.parameters = parameters;
    // }
    // public void setParameters(Parameter[] parameters) throws Exception {
    //     if(Utils.checkParameters(parameters)){
    //         this.parameters = parameters;
    //     } else{
    //         throw new IllegalArgumentException("ETU002610 // Les parametres de la methode doivent etre annotés");
    //     }
    // }


}
 