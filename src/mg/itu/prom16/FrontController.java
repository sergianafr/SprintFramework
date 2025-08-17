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
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import src.mg.itu.prom16.annotations.*;
import src.mg.itu.prom16.classes.ModelView;
import src.mg.itu.prom16.enumeration.Verbs;
import src.mg.itu.prom16.exceptions.InvalidParamValueException;
import src.mg.itu.prom16.exceptions.ReturnTypeException;
import src.mg.itu.prom16.exceptions.UnsupportedVerbException;
import src.mg.itu.prom16.mapping.Mapping;
import src.mg.itu.prom16.utils.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author SERGIANA
 */
@MultipartConfig
public class FrontController extends HttpServlet {
    protected Verbs verbRequest;
    protected HashMap<String,Mapping> urlMapping = new HashMap<String,Mapping>();
    public static String SESSION_AUTHENTICATED, SESSION_ROLE;
    protected String PROJECT_NAME;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    
    
    public void checkOutput(HttpServletRequest req, HttpServletResponse resp,
                            Method mappingMethod, Class<?> retour, Object result)
            throws ServletException, IOException {

        try (PrintWriter out = resp.getWriter()) {

            if (mappingMethod.isAnnotationPresent(Restapi.class)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");

                Gson gson = createGson();

                if (retour == ModelView.class) {
                    out.print(gson.toJson(((ModelView) result).getData()));
                } else {
                    out.print(gson.toJson(result));
                }
                out.flush();

            } else { // rendu JSP/HTML
                handleView(req, resp, retour, result);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    // Création du Gson avec TypeAdapters pour Date et Timestamp
    private Gson createGson() {
        JsonDeserializer<Date> dateDeserializer = (json, typeOfT, context) -> {
            try {
                return Date.valueOf(json.getAsString());
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        };
        JsonSerializer<Date> dateSerializer = (src, typeOfSrc, context) ->
                new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd").format(src));

        JsonDeserializer<Timestamp> timestampDeserializer = (json, typeOfT, context) -> {
            try {
                return Timestamp.valueOf(json.getAsString().replace("T", " "));
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        };
        JsonSerializer<Timestamp> timestampSerializer = (src, typeOfSrc, context) ->
                new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src));

        return new GsonBuilder()
                .registerTypeAdapter(Date.class, dateSerializer)
                .registerTypeAdapter(Date.class, dateDeserializer)
                .registerTypeAdapter(Timestamp.class, timestampSerializer)
                .registerTypeAdapter(Timestamp.class, timestampDeserializer)
                .create();
    }

    // Gestion du rendu JSP/ModelView
    private void handleView(HttpServletRequest req, HttpServletResponse resp,
                            Class<?> retour, Object result) throws ServletException, IOException {

        if (retour == String.class) {
            resp.getWriter().println((String) result);

        } else if (retour == ModelView.class) {
            ModelView mv = (ModelView) result;
            req.setAttribute("attribut", mv.getData());

            RequestDispatcher dispatcher = req.getRequestDispatcher(mv.getUrl());
            dispatcher.forward(req, resp);

        } else {
            throw new ReturnTypeException("The return type is not supported.");
        }
    }
    
     public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(isStaticFile(req, resp)){
            return;
        }
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        try {
            System.out.println("REQUEST URI " + req.getRequestURI());
            System.out.println("CONTEXT PATHHHH: " + req.getContextPath());
            // getting the URL requested 
            String contextPath = req.getContextPath(); // => "/PROJECT_NAME"
            String uri = req.getRequestURI();          // => "/PROJECT_NAME/login/check"
            String urlToSearch = uri.substring(contextPath.length()); // => "/login/check"
            if (urlToSearch.startsWith("/")) {
                urlToSearch = urlToSearch.substring(1);
            }
            System.out.println(urlToSearch + ": ------requested URL!!!!");

            // Finding the url dans le map
            if(urlMapping.containsKey(urlToSearch)) {
                Errors errors = new Errors();
                Mapping m = urlMapping.get(urlToSearch);
                
                Method mappingMethod = m.getMethod(verbRequest);

                // setting where it redirects in case of error in validation of parameters 
                if(mappingMethod.isAnnotationPresent(ErrorPage.class)){
                    errors.setRedirectionUrl(mappingMethod.getAnnotation(ErrorPage.class).url());
                }
                Class<?> retour = m.getReturnType(verbRequest);
                Object result = m.invoke(verbRequest, req, errors);

            
                checkOutput(req, resp, mappingMethod, retour, result);                

            } else {
                out.println("The method requested is not found : '" + urlToSearch );
                out.flush();
            }
            
            out.flush();
            out.close();
        } catch(InvalidParamValueException ipv){
            // Getting the errors
            Errors errors = ipv.getErrors();
            // Getting the redirection url
            String redirectionUrl = errors.getRedirectionUrl();
            // Setting the errors in the request
            req.setAttribute("errors", errors);
            // Redirecting to the error page
            RequestDispatcher dispatcher = req.getRequestDispatcher(redirectionUrl);
            dispatcher.forward(req, resp);
        }
        catch(UnsupportedVerbException ve){
            resp.sendError(404, ve.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            
            out.println(e.getMessage());
        }
    }

    protected boolean isStaticFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = url.substring(contextPath.length());
    
        // Définir les extensions autorisées pour les fichiers statiques
        String[] staticExtensions = {".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg", ".woff", ".woff2", ".ttf"};
    
        // Vérifier si l'URL correspond à un fichier statique
        for (String ext : staticExtensions) {
            if (relativePath.endsWith(ext)) {
                System.out.println(relativePath+" ---static file found");
                java.io.File staticFile = new java.io.File(getServletContext().getRealPath(relativePath));
                if (staticFile.exists() && staticFile.isFile()) {
                    // Déterminer le type MIME et renvoyer le fichier
                    String mimeType = getServletContext().getMimeType(staticFile.getName());
                    if (mimeType == null) {
                        mimeType = "application/octet-stream"; // Par défaut
                    }
                    resp.setContentType(mimeType);
                    resp.setContentLength((int) staticFile.length());
                    
                    // Envoyer le fichier dans la réponse
                    try (var in = new java.io.FileInputStream(staticFile);
                         var out = resp.getOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }   

    
    @Override
    public void init() throws ServletException {
        super.init();
        PROJECT_NAME = getInitParameter("project_name");
        SESSION_AUTHENTICATED = getInitParameter("session_authenticated") != null? getInitParameter("session_authenticated"): "authenticated";
        System.out.println(SESSION_AUTHENTICATED+" authenticated");
	    SESSION_ROLE = getInitParameter("session_role") != null ? getInitParameter("session_role") : "role";
        ServletContext context = getServletContext();
        String packageName = context.getInitParameter("Controllers");
        System.out.println("FrontController INITIALIZED SUCCESSFULLY!");
        
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

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
