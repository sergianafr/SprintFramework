/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package src.mg.itu.prom16;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.naming.directory.InvalidAttributesException;

import src.annotations.*;
import src.classes.ModelView;
import src.utils.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author SERGIANA
 */
public class FrontController extends HttpServlet {
    private List<String> listControllers;
    protected HashMap<String,Mapping> urlMapping = new HashMap<String,Mapping>();


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    
    
     public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        try {
            // getting the URL requested
            String requestedURL = req.getRequestURL().toString();
            String[] partedReq = requestedURL.split("/");
            String urlToSearch = partedReq[partedReq.length - 1];  
            System.out.println(requestedURL);  

            // Finding the url dans le map
            if(urlMapping.containsKey(urlToSearch)) {
                Mapping m = urlMapping.get(urlToSearch);
                Parameter[] params = m.getParameters();
                String[] args = this.getStringMethodArgs(params, req);

                // if(m.getParameters() != null) {
                //     System.out.println(m.getParameters());

                //     // Retrieve the parameters of the method from the request first 
                //     params = m.getParameters();
                //     args = new String[params.length];
                //     int i = 0;
                //     for (Parameter param : params) {
                //         String paramString = req.getParameter(param.getName());
                //         if(paramString != null){
                //             args[i] = req.getParameter(param.getName());
                //         } else {
                //             String paramName = param.getAnnotation(Param.class).name();
                //             args[i] = req.getParameter(paramName);
                //         }
                //     }
                // }
                
                // Invoking the method 
                Object result = m.invoke(args);
                if (result instanceof String){
                    out.println(result);
                } else if (result instanceof ModelView){
                    ModelView view = (ModelView)result; 
                    req.setAttribute("attribut", view.getData());

                    RequestDispatcher dispatcher = req.getRequestDispatcher(view.getUrl());
                    dispatcher.forward(req, resp);
                }

                
            } else {
                out.println("No method matching '" + urlToSearch + "' to call");
            }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println(e.getMessage()+"   exccc");
        }
    }

    protected Object[] getMethodArgs(Parameter[] params, HttpServletRequest req) throws Exception{
        try {
            if(params != null){
                Object[] args = new Object[params.length];
                int i = 0;
                for (Parameter param : params) {
                    if(!param.getType().isPrimitive()){
                        Class parameterType = param.getType();
                        String name = param.getName();
                        Vector<Object> attr = new Vector<Object>();
                        for(Field field : parameterType.getDeclaredFields()){
                            attr.add(req.getParameter(name+"."+field.getName()));
                        }
                        Constructor cons = parameterType.getConstructor(null);
                        Object obj = cons.newInstance(null);
                    }
                }
            }
        } catch (Exception e) {
           throw e;
        }
        return null;
    }

    proc

    // protected void settingAttribute(Class classe, Object obj){
    //     for(Field field : classe.getDeclaredFields()){
    //         String setter = 
    //         Method m = classe.getMethod("set"+fi);
    //     }
    // }

    
    protected String[] getStringMethodArgs(Parameter[] params, HttpServletRequest req){
        if(params != null){
            String[] methodArgs = new String[params.length];
            int i = 0;
            for (Parameter param : params) {
                String paramString = req.getParameter(param.getName());
                if(paramString != null){
                    methodArgs[i] = req.getParameter(param.getName());
                } else {
                    String paramName = param.getAnnotation(Param.class).name();
                    methodArgs[i] = req.getParameter(paramName);
                }
            }
            return methodArgs;
        }
        return null;
    }

    public List<Class<?>> findClasses(String packageName) throws ClassNotFoundException, InvalidAttributesException {
        List<Class<?>> classes = new ArrayList<>();

        // making sure the path to the controller package is correct
        String path = "WEB-INF/classes/" + packageName.replace(".", "/");
        String realPath = getServletContext().getRealPath(path);

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
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    
    @Override
    public void init() throws ServletException {
        super.init();

        // fetching the controller package's value from web.xml
        ServletContext context = getServletContext();
        String packageName = context.getInitParameter("Controllers");

        List<String> controllers = listControllers;
        controllers = new ArrayList<>(); // making sure the variable isn't null and emptying it everytime

        HashMap<String, Mapping> urls = urlMapping;
        urls = new HashMap<>(); // making sure the variable isn't null and emptying it everytime
        
        try {

            // fetching all classes in the controllers package
            List<Class<?>> allClasses = this.findClasses(packageName);

            for (Class<?> classe : allClasses) {
                // checking which of these classes are controllers
                if(classe.isAnnotationPresent(Controller.class)) {
                    controllers.add(classe.getName());
                    
                    // iterating through all the methods of the controller classes to check which ones are annotated with Get
                    Method[] allMethods = classe.getMethods();
                    for (Method m : allMethods) {
                        if (m.isAnnotationPresent(Get.class)) {
                            Get mGetAnnotation = (Get) m.getAnnotation(Get.class);
                            if(urls.containsKey(mGetAnnotation.url())){
                                throw new InvalidAttributesException("The url "+mGetAnnotation.url()+" is duplicated.");
                            }
                            // when a method is annotated with Get, we fetch its url value and create a new couple in the urlsToMethods Map
                            urls.put(mGetAnnotation.url(), new Mapping(classe.getName(), m.getName(), m.getParameters()));
                        }
                    }
                }
            }
            // setting the values of the attributes
            this.listControllers = controllers;
            this.urlMapping = urls;
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
                
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
