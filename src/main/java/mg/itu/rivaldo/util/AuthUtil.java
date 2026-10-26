package mg.itu.rivaldo.util;

import mg.itu.rivaldo.annotation.Auth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
}
    

    