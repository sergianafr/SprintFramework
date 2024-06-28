package src.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import src.annotations.Param;

public class Utils {
    public static Method setter(Field f, Class<?> clazz) throws NoSuchMethodException {
        Method m = null;
        String setterName = "set";

        String fieldName = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1).toLowerCase();

        setterName += fieldName;

        Class<?> paramType = f.getType();

        m = clazz.getDeclaredMethod(setterName, paramType);

        return m;
    }

    public static boolean checkParameters(Parameter[] params){
        for(Parameter p : params){
            if(!p.isAnnotationPresent(Param.class)){
                return false;
            }
        }
        return true;
    }
    public static boolean checkParameters(Method m) throws Exception{
        Parameter[] params = m.getParameters();
        for(Parameter p : params){
            if(!p.isAnnotationPresent(Param.class)){
                return false;
            }
        }
        return true;
    }
    public static Object convert(String value, Class<?> goalClass) {
        if(value == null) {
            return null;
        } else if(goalClass == int.class) {
            return Integer.parseInt(value);
        } else if(goalClass == float.class) {
            return Float.parseFloat(value);
        } else if(goalClass == double.class) {
            return Double.parseDouble(value);
        } else if(goalClass == long.class) {
            return Long.parseLong(value);
        } else if(goalClass == short.class) {
            return Short.parseShort(value);
        } else if(goalClass == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if(goalClass == char.class) {
            return value.charAt(0);
        } else {
            return value;
        }

    }

}