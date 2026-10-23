package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.itu.rivaldo.annotation.RestApi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import mg.itu.rivaldo.annotation.MapSession;
import mg.itu.rivaldo.util.BindingUtil;
import mg.itu.rivaldo.util.SessionUtil;
import mg.itu.rivaldo.annotation.MapSession;
import mg.itu.rivaldo.annotation.Auth;
import org.springframework.context.ApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.MultipartConfig;



@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 10 * 1024 * 1024
)
@WebServlet(name = "FrontController", urlPatterns = {"/"})
public class FrontControllerServlet extends HttpServlet {

    private String prefixe;
    private String suffixe;
    private List<String> controllerClassNames;
    private Map<UrlType, Mapping> mappingUrls = new HashMap<>();
    private ApplicationContext springContext;
    private String authSessionName;

    @Override   
    @SuppressWarnings("unchecked") 
    public void init() throws ServletException {
        try {
          ServletContext context = getServletContext();
          mappingUrls = (Map<UrlType, Mapping>) context.getAttribute("mappingUrls");
          controllerClassNames = (List<String>) context.getAttribute("controllerClassNames");
          prefixe = (String) context.getAttribute("prefixe");
          suffixe = (String) context.getAttribute("suffixe");
          springContext = (ApplicationContext) context.getAttribute("springContext");
          authSessionName = getServletContext().getInitParameter("auth-session");
          if(authSessionName == null || authSessionName.isBlank()) {
              throw new ServletException("Le paramètre d'initialisation 'auth-session' est manquant ou vide dans le fichier web.xml");
          }
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
            System.out.println("URL : " + path);
            Mapping mapping = mappingUrls.get(new UrlType("/" + path, request.getMethod()));

            if (mapping != null) {
                Object controllerInstance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                Object retour;

                // Vérifier l'authentification avant l'exécution
                if (mapping.getMethod().isAnnotationPresent(Auth.class)) {

                    HttpSession session = request.getSession(false);

                    if (session == null || session.getAttribute(authSessionName) == null) {
                        response.setContentType("text/html;charset=UTF-8");
                        response.getWriter().println(
                            "<p style='color:red;'>Accès refusé : l'utilisateur n'est pas authentifié.</p>"
                        );
                        return;
                    }
                }
                
                //verification si la méthode a des paramètres
                Class<?>[] parameterTypes = mapping.getMethod().getParameterTypes();

                MapSession mapSession = null;

                if (parameterTypes.length == 0) {
                    // Méthode sans paramètre
                    retour = mapping.getMethod().invoke(controllerInstance);

                } else {

                    Object[] parameters = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class<?> parameterType = parameterTypes[i];

                        // Cas ApplicationContext
                        if (ApplicationContext.class.isAssignableFrom(parameterType)) {
                            parameters[i] = springContext;
                        }

                        // Cas String
                        else if (parameterType == String.class) {
                            String parameterName = mapping.getMethod().getParameters()[i].getName();
                            parameters[i] = request.getParameter(parameterName);

                        }

                        // Cas int
                        else if (parameterType == int.class || parameterType == Integer.class) {
                            String parameterName = mapping.getMethod().getParameters()[i].getName();
                            String value = request.getParameter(parameterName);
                            parameters[i] = Integer.parseInt(value);

                        }

                        // Cas double
                        else if (parameterType == double.class || parameterType == Double.class) {
                            String parameterName = mapping.getMethod().getParameters()[i].getName();
                            String value = request.getParameter(parameterName);
                            parameters[i] = Double.parseDouble(value);

                        } 

                        else if (parameterType == MapSession.class) {
                            if (mapSession == null) {
                                mapSession = SessionUtil.fromHttpSession(request);
                            }
                            parameters[i] = mapSession;
                        }
                                                
                        // Objet
                        else{ 
                            Object paramObject = BindingUtil.bindObject(parameterType, request);
                            parameters[i] = paramObject;
                        }
                    }

                    retour = mapping.getMethod().invoke(controllerInstance, parameters);

                    if (mapSession != null) {
                        SessionUtil.syncWithHttpSession(request, mapSession);
                    }
                }

                if (mapping.getMethod().isAnnotationPresent(RestApi.class)) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    if (retour instanceof String) {
                        response.getWriter().write((String) retour);
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonResponse = objectMapper.writeValueAsString(retour);
                        response.getWriter().write(jsonResponse);
                    }  
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





