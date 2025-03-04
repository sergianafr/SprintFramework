package src.mg.itu.prom16.utils;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import src.mg.itu.prom16.FrontController;
import src.mg.itu.prom16.annotations.Authenticated;

public class AuthUtils {
    public static boolean isAuthorised(HttpServletRequest request, Authenticated authenticatedAnnotation) {
        HttpSession session = request.getSession();

        Object authenticatedObject = session.getAttribute(FrontController.SESSION_AUTHENTICATED);
        if (authenticatedObject == null)
            return false;

        boolean authenticated = (boolean) authenticatedObject;
        if (authenticatedAnnotation.roles().length == 0)
            return authenticated;

        Object roleObject = session.getAttribute(FrontController.SESSION_ROLE);
        if (roleObject == null)
            return false;

        String role = roleObject.toString();
        String[] authorisedRoles = authenticatedAnnotation.roles();
        return authenticated && Arrays.asList(authorisedRoles).contains(role);
    }
}
