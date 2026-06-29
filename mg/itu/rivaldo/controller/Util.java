package mg.itu.rivaldo.controller;

import mg.itu.rivaldo.controller.Mapping;
import mg.itu.rivaldo.controller.UrlType;
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
import java.util.Map;



public class Util {

    public List<String> getListClassNamesWithAnnotation(String packageName, Class annotationClass,Map<UrlType, Mapping> mappingUrls) {

    List<String> result = new ArrayList<>();
    String packagePath = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if (protocol.equals("file")) {
                    scanDirectory( new File(resource.toURI()),packageName,annotationClass,result,mappingUrls
                    );
                } else if (protocol.equals("jar")) {
                    String jarPath = resource.getPath();
                    jarPath = jarPath.substring(5, jarPath.indexOf("!"));
                    scanJar(jarPath, packagePath,annotationClass,result,mappingUrls,classLoader
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    

    private void scanDirectory(File directory,String packageName,Class annotationClass,List<String> result,Map<UrlType, Mapping> mappingUrls) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "."+ file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        result.add(clazz.getName());
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(UrlMethod.class)) {
                                UrlMethod annotation =
                                        method.getAnnotation(UrlMethod.class);
                                String url = annotation.value();
                                String verb = annotation.type();
                                mappingUrls.put(new UrlType(url, verb), new Mapping(clazz, method));
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void scanJar(String jarPath,String packagePath,Class annotationClass,List<String> result, Map<UrlType, Mapping> mappingUrls,ClassLoader classLoader) {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.replace("/", ".").replace(".class", "");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            result.add(clazz.getName());
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.isAnnotationPresent(UrlMethod.class)) {
                                    UrlMethod annotation = method.getAnnotation(UrlMethod.class);
                                    String url = annotation.value();
                                    String verb = annotation.type();
                                    mappingUrls.put(new UrlType(url, verb), new Mapping(clazz, method));
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Classe non chargeable : "+ className);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}