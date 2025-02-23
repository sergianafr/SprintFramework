/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package src.mg.itu.prom16;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;


import src.mg.itu.prom16.annotations.*;
import src.mg.itu.prom16.classes.ModelView;
import src.mg.itu.prom16.enumeration.Verbs;
import src.mg.itu.prom16.exceptions.ReturnTypeException;
import src.mg.itu.prom16.mapping.Mapping;
import src.mg.itu.prom16.utils.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author SERGIANA
 */
public class FrontController extends HttpServlet {
    protected Verbs verbRequest;
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

                Method mappingMethod = m.getMethod(verbRequest);
                Class<?> retour = m.getReturnType(verbRequest);

                Object result = m.invoke(verbRequest, req);

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
        this.verbRequest = Verbs.GET;
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
        this.verbRequest = Verbs.POST;
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
