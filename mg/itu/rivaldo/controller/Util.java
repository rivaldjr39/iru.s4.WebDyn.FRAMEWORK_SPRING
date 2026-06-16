package mg.itu.rivaldo.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public List<String> getListClassNamesWithAnnotation(
            String packageName,
            Class annotationClass) {

        List<String> result = new ArrayList<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            String path = packageName.replace('.', '/');

            URL resource = classLoader.getResource(path);

            if (resource == null) {
                System.out.println("Package introuvable: " + path);
                return result;
            }

            File directory = new File(resource.toURI());

            File[] files = directory.listFiles();

            if (files == null) return result;

            for (File file : files) {

                if (file.getName().endsWith(".class")) {

                    String className =
                            file.getName().replace(".class", "");

                    Class<?> clazz =
                            Class.forName(packageName + "." + className);

                    if (clazz.isAnnotationPresent(annotationClass)) {

                        result.add(clazz.getName());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}