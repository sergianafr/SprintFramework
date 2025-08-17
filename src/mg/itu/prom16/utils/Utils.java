package src.mg.itu.prom16.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.sql.Timestamp;
import jakarta.servlet.http.HttpSession;

import src.mg.itu.prom16.annotations.Param;
import src.mg.itu.prom16.classes.CustomSession;

public class Utils {
    public static Method setter(Field f, Class<?> clazz) throws NoSuchMethodException {
        Method m = null;
        String setterName = "set";

        // String fieldName = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1).toLowerCase();
        String fieldName = f.getName().substring(0, 1).toUpperCase()+ f.getName().substring(1);
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
            if(!p.isAnnotationPresent(Param.class) && p.getType() != CustomSession.class){
                return false;
            }
        }
        return true;
    }

    public static void emptySession(HttpSession session) {

        // Retrieving the values of the session
        Enumeration<String> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            session.removeAttribute(keys.nextElement());
        }
    }
    public static HashMap<String, Object> getSessionValues(HttpSession session) {
        HashMap<String, Object> values = new HashMap<String, Object>();

        // Retrieving the values of the session
        Enumeration<String> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            values.put(key, session.getAttribute(key));
        }

        return values;
    }

    public static Object convert(String value, Class<?> goalClass) {
        System.out.println("Converting " + value + " to " + goalClass.getName());
        if(value == null) {
            return null;
        } else if(goalClass == int.class) {
            return Integer.parseInt(value);
        }else if(goalClass == java.sql.Timestamp.class) {
            // Handle Timestamp in yyyy-MM-dd'T'HH:mm format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
            // Convert LocalDateTime to Timestamp
            ZoneId systemZone = ZoneId.systemDefault();
            Instant instant = localDateTime.atZone(systemZone).toInstant();
            Timestamp timestamp = Timestamp.from(instant);
            System.out.println("Converted Timestamp: " + timestamp);
            return timestamp;
        }  else if(goalClass == java.sql.Date.class) {
        // Handle Date in yyyy-MM-dd format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(value, formatter);
            // Convert LocalDate to java.sql.Date
            System.out.println("Converting to java.sql.Date " + java.sql.Date.valueOf(localDate));
            return java.sql.Date.valueOf(localDate);
        }else if(goalClass == float.class) {
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