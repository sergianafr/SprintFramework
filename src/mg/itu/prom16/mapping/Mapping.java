package src.mg.itu.prom16.mapping;
import src.mg.itu.prom16.annotations.File;
import src.mg.itu.prom16.annotations.NotBlank;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import src.mg.itu.prom16.annotations.Range;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import src.mg.itu.prom16.annotations.Param;
import src.mg.itu.prom16.annotations.Required;
import src.mg.itu.prom16.classes.CustomSession;
import src.mg.itu.prom16.enumeration.Verbs;
import src.mg.itu.prom16.exceptions.InvalidParamValue;
import src.mg.itu.prom16.exceptions.UnsupportedVerbException;
import src.mg.itu.prom16.utils.FilePart;
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
        }else{
            throw new UnsupportedVerbException(String.valueOf(verb));
        }
        // throw new UnsupportedVerbException(String.valueOf(verb));
        
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
            if(!p.isAnnotationPresent(Param.class) && p.getType() != CustomSession.class && !p.isAnnotationPresent(File.class)){
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
        Class<?> paramType = parameter.getType();
        Object result = null;
        
        // If the parameter is annotated with @File convert directly the part from the request to a FilePart object
        if(parameter.isAnnotationPresent(src.mg.itu.prom16.annotations.File.class)){
            if(paramType.equals(FilePart.class)){
                String key = parameter.getAnnotation(File.class).name();
                result = new FilePart(req.getPart(key), null);
            }
            else{
                throw new IllegalArgumentException("An object annotated with @File must be of type FilePart");
            }
        }
        String key = getParameterKey(parameter);
        
        if (paramType == CustomSession.class) {
            result = createCustomSession(req);
        }
        else if (isObject(paramType)) {
            result = createObject(req, key, paramType);
        } else {
            result = createSimpleObject(req, key, paramType);
        }
        try {
            checkValidationParam(parameter, result);
        } catch (Exception e) {
           throw e;
        }
        return result;
    }

    private void checkValidationParam(Parameter param, Object value) throws Exception{
        if(param.isAnnotationPresent(Required.class)){
            if(value == null){
                throw new InvalidParamValue("The parameter " + param.getName() + " is required");
            }
        } else if (param.isAnnotationPresent(NotBlank.class)){
            if(param.getType() == String.class && value == null || (String)value == ""){
                throw new InvalidParamValue("The parameter " + param.getName() + " is should not be blank");
            }
        } else if (param.isAnnotationPresent(Range.class)){
            Range range = param.getAnnotation(Range.class);
            if(value != null){
                if(value instanceof Integer){
                    if((int)value < range.minValue() || (int)value > range.maxValue()){
                        throw new InvalidParamValue("The parameter " + param.getName() + " should be between " + range.minValue() + " and " + range.maxValue());
                    }
                } else if(value instanceof Double){
                    if((double)value < range.minValue() || (double)value > range.maxValue()){
                        throw new InvalidParamValue("The parameter " + param.getName() + " should be between " + range.minValue() + " and " + range.maxValue());
                    }
                } else if(value instanceof Float){
                    if((float)value < range.minValue() || (float)value > range.maxValue()){
                        throw new InvalidParamValue("The parameter " + param.getName() + " should be between " + range.minValue() + " and " + range.maxValue());
                    }
                } else if(value instanceof Long){
                    if((long)value < range.minValue() || (long)value > range.maxValue()){
                        throw new InvalidParamValue("The parameter " + param.getName() + " should be between " + range.minValue() + " and " + range.maxValue());
                    }
                } else {
                    throw new InvalidParamValue("The parameter " + param.getName() + " should have a numerical value");
                }
            }
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
        System.out.println(key +" key");
        if (paramType == Double.class || paramType == double.class) {
            String value = req.getParameter(key);
            return (value != null) ? Double.valueOf(value) : 0.0;
        } else if (paramType == Float.class || paramType == float.class) {
            String value = req.getParameter(key);
            return (value != null) ? Float.valueOf(value) : 0.0f;
        } else if (paramType == Integer.class || paramType == int.class) {
            String value = req.getParameter(key);
            return (value != null) ? Integer.valueOf(value) : 0;
        }
        
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
 