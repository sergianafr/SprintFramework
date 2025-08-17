package src.mg.itu.prom16.mapping;
import src.mg.itu.prom16.annotations.Authenticated;
import src.mg.itu.prom16.annotations.File;
import src.mg.itu.prom16.annotations.NotBlank;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Authenticator;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import src.mg.itu.prom16.annotations.Range;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import src.mg.itu.prom16.annotations.Param;
import src.mg.itu.prom16.annotations.Public;
import src.mg.itu.prom16.annotations.Required;
import src.mg.itu.prom16.classes.CustomSession;
import src.mg.itu.prom16.enumeration.Verbs;
import src.mg.itu.prom16.exceptions.InvalidParamValueException;
import src.mg.itu.prom16.exceptions.UnauthorisedUserException;
import src.mg.itu.prom16.exceptions.UnsupportedVerbException;
import src.mg.itu.prom16.utils.AuthUtils;
import src.mg.itu.prom16.utils.Errors;
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
        // System.out.println(verbMethod.get(Verbs.GET) + " : method");
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
    public Object[] findParams(HttpServletRequest req, Verbs requestVerb, Errors listErrors) throws Exception {
        List<Object> args = new ArrayList<>();
        Method method = verbMethod.get(requestVerb);
    
        try {
            for (Parameter parameter : method.getParameters()) {
                Object arg = processParameter(req, parameter, listErrors);
                args.add(arg);
            }
            if(!listErrors.isEmpty()){
                throw new InvalidParamValueException(listErrors);
            }
        } catch (Exception e) {
            throw e;
        }
    
        return args.isEmpty() ? null : args.toArray();
    }
    
    private Object processParameter(HttpServletRequest req, Parameter parameter, Errors listErrors) throws Exception {
        Class<?> paramType = parameter.getType();
        Object result = null;
        
        // If the parameter is annotated with @File convert directly the part from the request to a FilePart object
        if(parameter.isAnnotationPresent(src.mg.itu.prom16.annotations.File.class)){
            if(paramType.equals(FilePart.class)){
                String key = parameter.getAnnotation(File.class).name();
                result = new FilePart(req.getPart(key), req.getServletContext());
                return result;
            }
            else{
                throw new InvalidParamValueException("An object annotated with @File must be of type FilePart");
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
        // Checking that the parameters are valid
        checkValidationParam(parameter, result, listErrors);
        // if there are errors related to the validation of the parameters, throw an InvalidParamValueException containing the list of errors 
        return result;
    }

    private void checkValidationParam(Parameter param, Object value, Errors listErrors) {
        if(param.isAnnotationPresent(Required.class)){
            System.out.println("Required"+" "+param.getName());
            if(value == null){

                System.out.println("The parameter " + param.getName() + " is required");
                listErrors.addError(param.getName(),new InvalidParamValueException("The parameter " + param.getName() + " is required").getMessage());
                // throw new InvalidParamValueException("The parameter " + param.getName() + " is required");
            }
        }if (param.isAnnotationPresent(NotBlank.class)){
            if(param.getType() == String.class && value == null || (String)value == ""){
                listErrors.addError(param.getName(),new InvalidParamValueException("The parameter " + param.getName() + " should not be blank").getMessage());
                System.out.println("The parameter " + param.getName() + " is should not be blank"   );
                // throw new InvalidParamValueException("The parameter " + param.getName() + " is should not be blank");
            }
        } if (param.isAnnotationPresent(Range.class)){
            Range range = param.getAnnotation(Range.class);
            if(value != null){
                if(value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof Long){
                    if((int)value < range.minValue() || (int)value > range.maxValue()){
                        listErrors.addError(param.getName(),new InvalidParamValueException("The parameter " + param.getName() + " should be between " + range.minValue() + " and " + range.maxValue()).getMessage());
                    }
                }  else {
                    listErrors.addError(param.getName(),new InvalidParamValueException("The parameter " + param.getName() + " should have a numerical value").getMessage());
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
        System.out.println(key + " : key");
        System.out.println(req.getParameter(key) + " : string value");

        
        String value = req.getParameter(key);
        // Traitement des types primitifs et wrapper (Double, Float, Integer, etc.)
        if (paramType == Double.class || paramType == double.class) {
        
            // Vérifier si la valeur est vide ou nulle et retourner null dans ce cas
            if (value == null || value.isEmpty()) {
                System.out.println("Value is null or empty");
                return null;
            }
            return Double.valueOf(value);
        } else if (paramType == Float.class || paramType == float.class) {
            // / Vérifier si la valeur est vide ou nulle et retourner null dans ce cas
            if (value == null || value.isEmpty()) {
                System.out.println("Value is null or empty");
                return null;
            }
            return Float.valueOf(value);
        } else if (paramType == Integer.class || paramType == int.class) {
            // / Vérifier si la valeur est vide ou nulle et retourner null dans ce cas
            if (value == null || value.isEmpty()) {
                System.out.println("Value is null or empty");
                return null;
            }
            return Integer.valueOf(value);
        } else if (paramType == Long.class || paramType == long.class) {
            // / Vérifier si la valeur est vide ou nulle et retourner null dans ce cas
            if (value == null || value.isEmpty()) {
                System.out.println("Value is null or empty");
                return null;
            }
            return Long.valueOf(value);
        } else if (paramType == java.sql.Timestamp.class) {
            // / Vérifier si la valeur est vide ou nulle et retourner null dans ce cas
            if (value == null || value.isEmpty()) {
                System.out.println("Value is null or empty");
                return null;
            }
            // Handle Timestamp in yyyy-MM-dd'T'HH:mm format
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
            // Convert LocalDateTime to Timestamp
            ZoneId systemZone = ZoneId.systemDefault();
            Instant instant = localDateTime.atZone(systemZone).toInstant();
            Timestamp timestamp = Timestamp.from(instant);
            System.out.println("Converted Timestamp: " + timestamp);
            System.out.println("Timestamp value: " + timestamp);
            return timestamp;
        }else if(paramType == java.sql.Date.class) {
        // Handle Date in yyyy-MM-dd format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(value, formatter);
            // Convert LocalDate to java.sql.Date
            System.out.println("Converting to java.sql.Date " + java.sql.Date.valueOf(localDate));
            return java.sql.Date.valueOf(localDate);
        }else if(paramType == Boolean.class || paramType == boolean.class){
            // / Vérifier si la valeur est vide ou nulle et retourner null dans ce cas
            if (value == null || value.isEmpty()) {
                System.out.println("Value is null or empty");
                return false;
            }
            System.out.println("Boolean value: "+Boolean.valueOf(value));
            return Boolean.valueOf(value);
        }

        // Traitement pour d'autres types d'objets
        Object obj = paramType.getDeclaredConstructor().newInstance();
        Field[] fields = paramType.getDeclaredFields();

        for (Field field : fields) {
            String fieldKey = buildFieldKey(key, field);
            String fieldValue = req.getParameter(fieldKey);

            // Si la valeur du champ est vide ou nulle, on ne l'affecte pas
            if (fieldValue != null && !fieldValue.isEmpty()) {
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
            System.out.println(fieldKey + " : fieldKey");
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

    public Object invoke(Verbs requestVerb, HttpServletRequest request, Errors listError)throws Exception{
        Class<?> controller =  this.getMethod(requestVerb).getDeclaringClass();
        Method m = verbMethod.get(requestVerb);

        if (
            controller.isAnnotationPresent(Authenticated.class)
                && !m.isAnnotationPresent(Public.class)
                && !m.isAnnotationPresent(Authenticated.class)
        ) {
            Authenticated authenticated = controller.getAnnotation(Authenticated.class);
            if (!AuthUtils.isAuthorised(request, authenticated)) throw new UnauthorisedUserException("You are not allowed to access this URL");
        }

        if(m.isAnnotationPresent(Authenticated.class)) {
            Authenticated authenticated = m.getAnnotation(Authenticated.class);
            if (!AuthUtils.isAuthorised(request, authenticated)) throw new UnauthorisedUserException("You are not allowed to access this URL");
        }
        Object o = null;
        try {
            checkParam(requestVerb);

            o = m.invoke(controllerClass.getConstructor().newInstance(), findParams((HttpServletRequest) request, requestVerb, listError));
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
 