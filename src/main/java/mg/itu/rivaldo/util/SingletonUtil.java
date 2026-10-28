package mg.itu.rivaldo.util;

import jakarta.servlet.ServletContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonUtil {

    private static final String ATTRIBUTE_NAME = "controllerInstances";

    @SuppressWarnings("unchecked")
    private static Map<Class<?>, Object> getMap(ServletContext context) {
        synchronized (context) {
            Map<Class<?>, Object> instances =
                    (Map<Class<?>, Object>) context.getAttribute(ATTRIBUTE_NAME);
            if (instances == null) {
                instances = new ConcurrentHashMap<>();
                context.setAttribute(ATTRIBUTE_NAME, instances);
            }
            return instances;
        }
    }

    public static Object getInstance(ServletContext context, Class<?> clazz) throws Exception {
        Map<Class<?>, Object> instances = getMap(context);

        Object instance = instances.get(clazz);
        if (instance == null) {
            synchronized (instances) {
                instance = instances.get(clazz); // on revérifie dans le bloc synchronisé
                if (instance == null) {
                    instance = clazz.getDeclaredConstructor().newInstance();
                    instances.put(clazz, instance);
                }
            }
        }
        return instance;
    }
}
