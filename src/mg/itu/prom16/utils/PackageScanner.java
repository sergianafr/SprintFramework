package src.mg.itu.prom16.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import src.mg.itu.prom16.annotations.Controller;
import src.mg.itu.prom16.annotations.EndPoint;
import src.mg.itu.prom16.annotations.Get;
import src.mg.itu.prom16.annotations.Post;
import src.mg.itu.prom16.annotations.Url;
import src.mg.itu.prom16.enumeration.Verbs;
import src.mg.itu.prom16.mapping.Mapping; 
public class PackageScanner {

    public static List<Class<?>> findControllerClasses(String packageName, String realPath) throws ClassNotFoundException, InvalidAttributesException {
        List<Class<?>> classes = new ArrayList<>();
        
        File directory = new File(realPath);
        File[] files = directory.listFiles();
        if(!directory.exists()){
            throw new InvalidAttributesException("The package "+packageName+" does not exist.");
        }
        else if(files.length <= 0){
            throw new InvalidAttributesException("The package "+packageName+" is empty.");
        } 
        for(File f : files) {
            // filtering class files
            if(f.isFile() && f.getName().endsWith(".class")) {
                String className = packageName + "." + f.getName().split(".class")[0];
                Class<?> packageClass = Class.forName(className);
                // checking if it's a controller 
                if(packageClass.isAnnotationPresent(Controller.class)) {
                    classes.add(packageClass);
                }            
            }
        }

        return classes;
    }

    public static HashMap<String, Mapping> getMapping(List<Class<?>> classes)throws ServletException{
        HashMap<String, Mapping> urlMapping = new HashMap<String, Mapping>();
        try {
            for (Class<?> classe : classes) {

                Method[] allMethods = classe.getMethods();
                for (Method m : allMethods) {
                    // Cheking if the method is an endpoint
                    if (m.isAnnotationPresent(EndPoint.class)) {
                        // Getting the url and the verb
                        String url = getUrl(m);
                        Verbs verb = getVerb(m);
                        Mapping mapping = new Mapping(classe, null);

                        // Making sure that the url isn't already mapped by another controller 
                        if(urlMapping.containsKey(url) && !urlMapping.get(url).getControllerClass().equals(classe)){
                            throw new InvalidAttributesException("The url "+url+" is mapped by different controllers.");
                        }
                        
                        mapping.setVerbMethod(verb, m);
                        urlMapping.put(url, mapping);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
        return urlMapping;
    }

    private static String getUrl(Method m){
        // Url mGetAnnotation = (Url) m.getAnnotation(Url.class);
        // return mGetAnnotation.url();
        return m.getAnnotation(EndPoint.class).url();
    }
    private static Verbs getVerb(Method m){
        return m.isAnnotationPresent(Post.class) ? Verbs.POST : Verbs.GET;
    }


    
    // public static List<Class<?>> getClasses(String packageName, ServletContext context) throws ClassNotFoundException, IOException {
    //     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    //     assert classLoader != null;
    //     String path = "WEB-INF/classes/"+packageName.replace('.', '/');
    //     path = context.getRealPath(path); 
    //     Enumeration<URL> resources = classLoader.getResources(path);
    //     List<File> dirs = new ArrayList<>();
    //     while (resources.hasMoreElements()) {
    //         URL resource = resources.nextElement();
    //         dirs.add(new File(resource.getFile()));
    //     }
    //     ArrayList<Class<?>> classes = new ArrayList<>(); 
    //     for (File directory : dirs) {
    //         classes.addAll(findClasses(directory, packageName));
    //     }
    //     return classes;
    // }

    // private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
    //     List<Class<?>> classes = new ArrayList<>();
    //     if (!directory.exists()) {
    //         return classes;
    //     }
    //     File[] files = directory.listFiles();
    //     if (files == null) {
    //         return classes;
    //     }
    //     for (File file : files) {
    //         if (file.isDirectory()) {
    //             assert !file.getName().contains(".");
    //             classes.addAll(findClasses(file, packageName + "." + file.getName()));
    //         } else if (file.getName().endsWith(".class")) {
    //             classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
    //         }
    //     }
    //     return classes;
    // }
}
