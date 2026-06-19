package mg.itu.rivaldo.controller;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.IOException;
import java.lang.reflect.Method;
import mg.itu.rivaldo.annotation.UrlMethod;

public class Util {

    public List<String> getListClassNamesWithAnnotation(String packageName,Class annotationClass) {

        List<String> result = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println("Resource URL: " + resource);
                String protocol = resource.getProtocol();
                if (protocol.equals("file")) {
                    scanDirectory(new File(resource.toURI()), packageName, annotationClass, result);
                } else if (protocol.equals("jar")) {
                    String jarPath = resource.getPath();
                    jarPath = jarPath.substring(5, jarPath.indexOf("!")); 
                    scanJar(jarPath, packagePath, packageName, annotationClass, result, classLoader);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    private void scanDirectory(File directory, String packageName,Class annotationClass, List<String> result) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        result.add(clazz.getName());
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Classe non trouvée : " + className);
                }
            }
        }
    }

    private void scanJar(String jarPath, String packagePath, String packageName,Class annotationClass, List<String> result, ClassLoader classLoader) {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName
                            .replace("/", ".")
                            .replace(".class", "");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            result.add(clazz.getName());
                            System.out.println("Classe annotée trouvée : " + className);
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Classe non chargeable : " + className);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findMethodByUrl(List<String> controllerClassNames, String path) throws Exception {

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

        return "Aucune méthode trouvée, les methodes disponibles sont : " + getAllAnnotatedMethods(controllerClassNames);
    }

    private String getAllAnnotatedMethods(List<String> controllerClassNames) throws Exception {
        List<String> methodsList = new ArrayList();

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
    
}