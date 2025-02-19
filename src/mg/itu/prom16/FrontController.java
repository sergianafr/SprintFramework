/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package src.mg.itu.prom16;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
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
import com.google.gson.Gson;

import javax.naming.directory.InvalidAttributesException;

import src.mg.itu.prom16.annotations.*;
import src.mg.itu.prom16.classes.CustomSession;
import src.mg.itu.prom16.classes.ModelView;
import src.mg.itu.prom16.exceptions.ReturnTypeException;
import src.mg.itu.prom16.mapping.Mapping;
import src.mg.itu.prom16.utils.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    
     public void checkOutput(HttpServletRequest req, HttpServletResponse resp, Method mappingMethod, Class<?> retour, Object result) throws ServletException, IOException {
        try {
            PrintWriter out = resp.getWriter();
            if (mappingMethod.isAnnotationPresent(Restapi.class)){     
                Gson gson = new Gson();
                
                // Retrieving the json value of the object returned by the method
                if(retour == ModelView.class) {
                    // System.out.println(gson.toJson(((ModelView)result).getData()));
                    out.print(gson.toJson(((ModelView)result).getData()));
                    out.flush();
                }else {
                    System.out.print(gson.toJson(result));
                    out.print(gson.toJson(result));       
                    out.flush();          
                }
                resp.setContentType("text/json");
                resp.setCharacterEncoding("UTF-8");
            } else {
                if(retour == String.class) {
                    out.println((String) result);
                } else if(retour == ModelView.class) {
                    ModelView mv = (ModelView) result;
                    req.setAttribute("attribut", mv.getData());
    
                    RequestDispatcher dispatcher = req.getRequestDispatcher(mv.getUrl());
                    dispatcher.forward(req, resp);
                }else {
                    throw new ReturnTypeException("The return type is not supported.");
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
     }
    
     public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        try {
            // getting the URL requested 
            String requestedURL = req.getRequestURL().toString();
            String[] partedReq = requestedURL.split("/");
            String urlToSearch = partedReq[partedReq.length - 1];  
            System.out.println(requestedURL+"ggggggg");  

            // Finding the url dans le map
            if(urlMapping.containsKey(urlToSearch)) {
                Mapping m = urlMapping.get(urlToSearch);
                // checking if all the parameters are annotated
                m.checkParam();

                Object[] args = this.findParams(req, m);
                Object result = m.invoke(args);
                Class<?> retour = m.getReturnType();

                Class mappingClass = Class.forName(m.getClassName());
                Method mappingMethod = mappingClass.getDeclaredMethod(m.getMethodName(), m.getTypes());
                
                if (mappingMethod == null) {
                    throw new ServletException("Method does not exist ");
                } 

                checkOutput(req, resp, mappingMethod, retour, result);                

            } else {
                out.println("The method requested is not found : '" + urlToSearch );
                out.flush();
            }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            out.println(e.getMessage());
        }
    }

    public Object[] findParams(HttpServletRequest req, Mapping method) throws Exception {
        List<Object> args = new ArrayList<>();

        Class<?> clazz = Class.forName(method.getClassName());
        Method m = clazz.getMethod(method.getMethodName(), method.getTypes());

        try {

                for (int i = 0; i < method.getParameters().length; i++) {
                    Parameter p = method.getParameters()[i];
                    Object o = null;
                    String key = "";
        
                    if(p.isAnnotationPresent(Param.class)) {
                        Param annotationParam = (Param) p.getAnnotation(Param.class);
                        key = annotationParam.name();
                    }
                    // Alea fitsarana 1: toutes les  parametres doivent être annotées
                    
                    // } else {
                    //     key = p.getName();
                    // }
        
                    Class<?> paramType = p.getType();
                    if(paramType == CustomSession.class) {
                        CustomSession customSession = new CustomSession(req.getSession());
                        o = customSession;
                    }
                    else if(!paramType.isPrimitive() && paramType != String.class && paramType != CustomSession.class)  {
                        
                        Constructor c = paramType.getDeclaredConstructor();
                        o = c.newInstance();
        
                       
                        Field[] attributes = paramType.getDeclaredFields();
                        for (Field attr : attributes) {
                            try {
                                String attrKey = key + ".";
                                if(attr.isAnnotationPresent(src.mg.itu.prom16.annotations.Field.class)) {
                                    
                                    src.mg.itu.prom16.annotations.Field f = attr.getAnnotation(src.mg.itu.prom16.annotations.Field.class);
                                    attrKey += f.name();
                                } else {
                                    attrKey += attr.getName();
                                }
    
                                String attrValStr = req.getParameter(attrKey);
    
                                Method setter = Utils.setter(attr, paramType);
                                setter.invoke(o, Utils.convert(attrValStr, attr.getType()));
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    } else {
                        String valueStr = req.getParameter(key);
                        o = Utils.convert(valueStr, paramType);
                    }
                    
                    args.add(o);
                }
    
        } catch (Exception e) {
            throw e;
        }
        

        if(args.size() > 0) {
            return args.toArray();
        } else {
            return null;
        }
    }


    
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

    // getting all the classes in the given package 

    // public List<Class<?>> findClasses(String packageName) throws ClassNotFoundException, InvalidAttributesException {
    //     List<Class<?>> classes = new ArrayList<>();

        
    //     String path = "WEB-INF/classes/" + packageName.replace(".", "/");
    //     String realPath = getServletContext().getRealPath(path);

    //     File directory = new File(realPath);
    //     File[] files = directory.listFiles();
    //     if(!directory.exists()){
    //         throw new InvalidAttributesException("The package "+packageName+" does not exist.");
    //     }
    //     else if(files.length <= 0){
    //         throw new InvalidAttributesException("The package "+packageName+" is empty.");
    //     } 
    //     for(File f : files) {
    //         // filtering class files
    //         if(f.isFile() && f.getName().endsWith(".class")) {
    //             String className = packageName + "." + f.getName().split(".class")[0];
    //             classes.add(Class.forName(className));
    //         }
    //     }

    //     return classes;
    // }

    
    @Override
    public void init() throws ServletException {
        super.init();

        ServletContext context = getServletContext();
        String packageName = context.getInitParameter("Controllers");

        // Getting the real path of the package containing the controllers
        String path = "WEB-INF/classes/" + packageName.replace(".", "/");
        String realPath = getServletContext().getRealPath(path);
        
        try {
            List<Class<?>> allClasses = PackageScanner.findControllerClasses(packageName, realPath);
            this.urlMapping = PackageScanner.getMapping(allClasses);
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
