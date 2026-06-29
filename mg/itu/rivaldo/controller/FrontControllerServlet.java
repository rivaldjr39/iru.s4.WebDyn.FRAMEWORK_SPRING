// FrontControllerServlet.java - version corrigée
package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import mg.itu.rivaldo.annotation.Url;
import mg.itu.rivaldo.annotation.UrlMethod;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;

public class FrontControllerServlet extends HttpServlet {

    private Util util = new Util();
    private List<String> controllerClassNames;
    private Map<UrlType, Mapping> mappingUrls = new HashMap<>();
   

    @Override
    public void init() throws ServletException {
        try {
            controllerClassNames = util.getListClassNamesWithAnnotation(
                "mg.itu.rivaldo.annotation",Url.class,mappingUrls);
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation", e);
        }
    }


    

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        String uri = request.getRequestURI();
        String path = uri.substring(request.getContextPath().length());
        path = path.substring(1);
        PrintWriter out = response.getWriter();
        try {
            out.println("URL    : " + path);
            Mapping mapping = mappingUrls.get(new UrlType("/" + path, request.getMethod()));
            if (mapping != null) {
                Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                Object result = mapping.getMethod().invoke(controllerInstance);
                out.println("URL:"+ path + "  Class :" + mapping.getControllerClass().getSimpleName() + "  -> " + mapping.getMethod().getName());
            
            } else {
                out.println("Aucune correspondance trouvée pour l'URL : " + path);
                out.println("Les methodes disponibles sont :");
                for (Map.Entry<UrlType, Mapping> entry : mappingUrls.entrySet()) {
                    UrlType urlType = entry.getKey();
                    Mapping m = entry.getValue();
                    out.println("URL: " + urlType.getUrl() + "  Class: " + m.getControllerClass().getSimpleName() + "  -> " + m.getMethod().getName());
                }
            }
        } catch (Exception e) {
            out.println("Erreur lors du traitement de la requête : " + e.getMessage());
        }
            
        out.println("---Sprint1---");
        for (String className : controllerClassNames) {
            out.println("Classe trouvée : " + className);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }
}