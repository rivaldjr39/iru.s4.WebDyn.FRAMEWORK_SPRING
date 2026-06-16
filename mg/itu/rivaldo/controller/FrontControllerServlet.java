package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import mg.itu.rivaldo.annotation.Url;
import java.util.List;
import mg.itu.rivaldo.controller.Util;

public class FrontControllerServlet extends HttpServlet {
    private Util util = new Util();
    List<String> controllerClassNames;

    public void init() throws ServletException {
        try {
            controllerClassNames = util.getListClassNamesWithAnnotation("mg.itu.rivaldo.annotation", Url.class);
        } catch (Exception e) {
            throw new ServletException("Erreur lors de la récupération des classes avec l'annotation @Url", e);
        }
    }

    protected void processRequest(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {response.setContentType("text/plain");

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        PrintWriter out = response.getWriter();
        out.println("URL recue : " + uri);
        out.println("Chemin : " + path);
        for (String className : controllerClassNames) {
           out.println("Classe trouvée : " + className);
        }
    }

    
    protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
                processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException, IOException {
        processRequest(request, response);
    }
}