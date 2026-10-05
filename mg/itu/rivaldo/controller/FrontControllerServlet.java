// FrontControllerServlet.java - version corrigée
package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.rivaldo.annotation.RestApi;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.context.ApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet(name = "FrontController", urlPatterns = {"/"})
public class FrontControllerServlet extends HttpServlet {

    private String prefixe;
    private String suffixe;
    private List<String> controllerClassNames;
    private Map<UrlType, Mapping> mappingUrls = new HashMap<>();
    private ApplicationContext springContext;

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
                
                //verification si la méthode a des paramètres
                Class<?>[] parameterTypes = mapping.getMethod().getParameterTypes();

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

                        // parametre de type objet (classe)
                        else {
                            Object obj = parameterType.getDeclaredConstructor().newInstance();
                            Method[] objMethods = parameterType.getDeclaredMethods();

                            for (Method method : objMethods) {
                                String methodName = method.getName();

                                if (methodName.startsWith("set") && method.getParameterCount() == 1) {
                                    String propertyName = methodName.substring(3);
                                    propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                                    String parameterValue = request.getParameter(propertyName);

                                    if (parameterValue == null) {
                                        continue;
                                    }
                                    
                                    Class<?> settertype = method.getParameterTypes()[0];
                                    Object convertedValue = convertValue(parameterValue, settertype);
                                    method.invoke(obj, convertedValue);
                                }
                            }
                            parameters[i] = obj;

                        }
                    }

                    retour = mapping.getMethod().invoke(controllerInstance, parameters);
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
    private Object convertValue(String value, Class<?> type) {

    if (type == String.class) {
        return value;
    }

    if (type == int.class || type == Integer.class) {
        return Integer.parseInt(value);
    }

    if (type == double.class || type == Double.class) {
        return Double.parseDouble(value);
    }

    if (type == long.class || type == Long.class) {
        return Long.parseLong(value);
    }

    if (type == float.class || type == Float.class) {
        return Float.parseFloat(value);
    }

    if (type == boolean.class || type == Boolean.class) {
        return Boolean.parseBoolean(value);
    }

    if (type == short.class || type == Short.class) {
        return Short.parseShort(value);
    }

    if (type == byte.class || type == Byte.class) {
        return Byte.parseByte(value);
    }

    if (type == char.class || type == Character.class) {
        return value.charAt(0);
    }

    throw new IllegalArgumentException(
        "Type non supporté : " + type.getName()
    );
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






