package mg.itu.rivaldo.util;

import mg.itu.rivaldo.annotation.Auth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;

public class AuthUtil {
    
    public static boolean verifyAuth(HttpServletRequest request, HttpServletResponse response, String authSessionName) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(authSessionName) == null) {

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(
                "<p style='color:red;'>Accès refusé : l'utilisateur n'est pas authentifié.</p>"
            );

            return false;
            
        }
        return true;
    }

    public static boolean verifyRole(HttpServletRequest request, HttpServletResponse response, String rolesSessionName, Auth auth) throws Exception {
        List<String> rolesAutorises = getRolesFromAuthAnnotation(auth.role());

        if (rolesAutorises.isEmpty()) {
            return true; // Pas de rôle requis, accès autorisé
        }

        HttpSession session = request.getSession(false);

        String roleUser = null;
        if (session != null) {
           Object roleObj = session.getAttribute(rolesSessionName);
           if (roleObj != null) {
               roleUser = roleObj.toString();
           }
        }

        for(String role : rolesAutorises) {
            if (role.equals(roleUser)) {
                return true; // Rôle autorisé, accès autorisé
            }
        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(
            "<p style='color:red;'>Accès refusé : l'utilisateur n'a pas le rôle requis.</p>"
        );

        return false;
            
    }

    private  static List<String> getRolesFromAuthAnnotation(String roles) {
        List<String> roleList = new ArrayList<>();
        if (roles != null && !roles.isEmpty()) {
            String[] rolesArray = roles.split(",");
            for (String role : rolesArray) {
                roleList.add(role.trim());
            }
        }
        return roleList;
    } 
}
    

    