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

import java.util.ArrayList;

public class FrontControllerServlet extends HttpServlet {

    private Util util = new Util();
    private List<String> controllerClassNames;

    @Override
    public void init() throws ServletException {
        try {
            controllerClassNames = util.getListClassNamesWithAnnotation(
                "mg.itu.rivaldo.annotation",  // ✅ package du Controller dans la JAR
                Url.class
            );
            System.out.println("Classes trouvées : " + controllerClassNames);
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation", e);
        }
    }


    private String findMethodByUrl(String path) throws Exception {

        for (String className : controllerClassNames) {

            Class<?> clazz = Class.forName(className);

            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {

                if (method.isAnnotationPresent(UrlMethod.class)) {

                    UrlMethod urlMethod = method.getAnnotation(UrlMethod.class);

                    String annotationUrl = urlMethod.value();

                    // comparaison du path avec l'annotation
                    if (annotationUrl.equals("/" + path)) {

                        return clazz.getSimpleName() + " -> " + method.getName();
                    }
                }
            }
        }

        return "Aucune méthode trouvée, les methodes disponibles sont : " + getAllAnnotatedMethods();
    }

    private String getAllAnnotatedMethods() throws Exception {
        List<String> methodsList = new ArrayList<>();

        for (String className : controllerClassNames) {
            Class<?> clazz = Class.forName(className);
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMethod.class)) {
                    UrlMethod urlMethod = method.getAnnotation(UrlMethod.class);
                    methodsList.add(clazz.getSimpleName() + " -> " + method.getName() + " (URL: " + urlMethod.value() + ")");
                }
            }
        }
        return String.join(", ", methodsList);
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
            String result = findMethodByUrl(path);
            out.println("Resultat  class et methode annotée   : " + result);
        } catch (Exception e) {
            out.println("Erreur lors de la recherche de la méthode : " + e.getMessage());
        }
        
        out.println("---");
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