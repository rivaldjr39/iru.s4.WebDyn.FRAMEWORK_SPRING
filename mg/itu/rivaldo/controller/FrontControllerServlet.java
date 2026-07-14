// FrontControllerServlet.java - version corrigée
package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.context.WebApplicationContext;


@WebServlet(name = "FrontController", urlPatterns = {"/"})
public class FrontControllerServlet extends HttpServlet {

    private String prefixe;
    private String suffixe;
    private List<String> controllerClassNames;
    private Map<UrlType, Mapping> mappingUrls = new HashMap<>();
    private WebApplicationContext springContext;

    @Override   
    @SuppressWarnings("unchecked") 
    public void init() throws ServletException {
        try {
          ServletContext context = getServletContext();
          mappingUrls = (Map<UrlType, Mapping>) context.getAttribute("mappingUrls");
          controllerClassNames = (List<String>) context.getAttribute("controllerClassNames");
          prefixe = (String) context.getAttribute("prefixe");
          suffixe = (String) context.getAttribute("suffixe");
          springContext = (WebApplicationContext) context.getAttribute("springContext");
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                Object retour;
                //verification si la méthode a des paramètres
                Class<?>[] parameterTypes = mapping.getMethod().getParameterTypes();
                if(parameterTypes.length == 0){
                    retour = mapping.getMethod().invoke(controllerInstance);
                }else if(parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(springContext.getClass())){
                    retour = mapping.getMethod().invoke(controllerInstance, springContext);
                }else {
                    out.println("La méthode " + mapping.getMethod().getName() + " de la classe " + mapping.getControllerClass().getSimpleName() + " a des paramètres non supportés.");
                    return;
                }
                if(retour instanceof ModelAndVue) {
                    ModelAndVue modelAndVue = (ModelAndVue) retour;
                    
                    for(Map.Entry<String, Object> entry : modelAndVue.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    String chemin = prefixe + modelAndVue.getVue() + suffixe;
                    request.getRequestDispatcher(chemin).forward(request, response);
                    return;

                } else {
                    out.println("Retour de la méthode : " + retour);
                }

                out.println("URL:"+ path + "  Class :" + mapping.getControllerClass().getSimpleName() + "  -> " + mapping.getMethod().getName());
            
            } else {
                out.println("Aucune correspondance trouvée pour l'URL : " + path);
                out.println("Les methodes disponibles sont :");
                for (Map.Entry<UrlType, Mapping> entry : mappingUrls.entrySet()) {
                    UrlType urlType = entry.getKey();
                    Mapping m = entry.getValue();
                    out.println("URL: " + urlType.getUrl() + "  Class: " + m.getControllerClass().getSimpleName() + "  -> " + m.getMethod().getName() + "  Type: " + urlType.getVerb());
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






