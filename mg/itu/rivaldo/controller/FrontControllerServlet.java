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
    private Map<String,List<List<String>>> urlToMethodMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            controllerClassNames = util.getListClassNamesWithAnnotation(
                "mg.itu.rivaldo.annotation",  
                Url.class
            );
            urlToMethodMap = util.buildUrlToMethodMap(controllerClassNames, UrlMethod.class);
            System.out.println("Classes trouvées : " + controllerClassNames);
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
            boolean found = false;

            for (Map.Entry<String, List<List<String>>> entry : urlToMethodMap.entrySet()) {
                String className = entry.getKey();
                List<List<String>> methodInfoList = entry.getValue();
                for (List<String> methodInfo : methodInfoList) {
                    String url = methodInfo.get(0);
                    String methodName = methodInfo.get(1);
                    if (url.equals("/" + path)) {
                        out.println("URL    : " + url+"  Classe : " + className + "-> " + methodName);
                        found = true;
                    }
                }
            }
            if (!found) {
                out.println("Aucune méthode trouvée pour l'URL : " + path);
                out.println("Les méthodes disponibles sont :");
                for (Map.Entry<String, List<List<String>>> entry : urlToMethodMap.entrySet()) {
                    String className = entry.getKey();
                    List<List<String>> methodInfoList = entry.getValue();
                    for (List<String> methodInfo : methodInfoList) {
                        String url = methodInfo.get(0);
                        String methodName = methodInfo.get(1);
                        out.println("URL : " + url + "  Classe : " + className + "-> " + methodName);
                    }
                }
            }
            
        } catch (Exception e) {
            out.println("Erreur lors de la recherche de la méthode : " + e.getMessage());
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