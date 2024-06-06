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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private boolean checked=false;
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

        // getting the URL requested by the client
        String requestedURL = req.getRequestURL().toString();
        String[] partedReq = requestedURL.split("/");
        String urlToSearch = partedReq[partedReq.length - 1];
        
        // searching for that URL inside of our HashMap
        if(urlMapping.containsKey(urlToSearch)) {
            Mapping m = urlMapping.get(urlToSearch);
            try {
                Object result = m.invoke();
                if (result instanceof String){
                    out.println(result);
                } else if (result instanceof ModelView){
                    ModelView view = (ModelView)result; 
                    req.setAttribute("attribut", view.getData());
                    RequestDispatcher dispatcher = req.getRequestDispatcher(view.getUrl());
                    dispatcher.forward(req, resp);
                }
            } catch (Exception e) {
                out.println(e.getMessage());
            }
            
        } else {
            out.println("No method matching '" + urlToSearch + "' to call");
        }

        out.flush();
        out.close();
    }

    public List<Class<?>> findClasses(String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        // making sure the path to the controller package is correct
        String path = "WEB-INF/classes/" + packageName.replace(".", "/");
        String realPath = getServletContext().getRealPath(path);

        File directory = new File(realPath);
        File[] files = directory.listFiles();

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
                            // when a method is annotated with Get, we fetch its url value and create a new couple in the urlsToMethods Map
                            Get mGetAnnotation = (Get) m.getAnnotation(Get.class);
                            System.out.println(m.getName()+"jngngngngnngng");
                            urls.put(mGetAnnotation.url(), new Mapping(classe.getName(), m.getName()));
                        }
                    }
                }
            }
            // setting the values of the attributes
            this.listControllers = controllers;
            this.urlMapping = urls;
        } catch (Exception e) {
            
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
