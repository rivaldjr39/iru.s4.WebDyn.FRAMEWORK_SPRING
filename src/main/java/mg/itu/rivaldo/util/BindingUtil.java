package mg.itu.rivaldo.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Collection;
import jakarta.servlet.http.Part;
import mg.itu.rivaldo.annotation.Mapfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;


public class BindingUtil {

    public static Object bindObject(Class<?> objectType, HttpServletRequest request ) throws Exception {
        Object obj = objectType.getDeclaredConstructor().newInstance();
        bindFields(obj, objectType, request, "");
        return obj;
    }

    private static void bindFields(Object obj,Class<?> objectType,HttpServletRequest request,String prefix) throws Exception {
        Field[] fields = getAllFields(objectType);

        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            String fieldName = prefix.isEmpty()? field.getName(): prefix + "." + field.getName();

            if (fieldType == Mapfile.class) {
                Mapfile file = bindFile(request, fieldName);

                if (file != null) {
                    field.set(obj, file);
                }
            }
            else if (List.class.isAssignableFrom(fieldType) && isListOfMapfile(field)) {
                List<Mapfile> files = bindFiles(request, fieldName);
                if (!files.isEmpty()) {
                    field.set(obj, files);
                }
            }
            else if (isSimpleType(fieldType)) {
                String value = request.getParameter(fieldName);
                if (value == null || value.isEmpty()) {
                    continue;
                }
                Object convertedValue = convertValue(value, fieldType);
                field.set(obj, convertedValue);
            }
            // 2. Attribut de type objet
            else if (!fieldType.isArray() && !Collection.class.isAssignableFrom(fieldType) && !Map.class.isAssignableFrom(fieldType)&& !fieldType.isEnum()) {
                String nestedPrefix = fieldName + ".";
                boolean hasParameter = request.getParameterMap().keySet().stream().anyMatch(name -> name.startsWith(nestedPrefix));
                if (!hasParameter) {
                    continue;
                }
                Object childObject = fieldType.getDeclaredConstructor().newInstance();
                bindFields(childObject,fieldType,request,fieldName);
                field.set(obj, childObject);
            }
        }
    }

    
    //Récupère les champs de la classe et de ses classes parentes.
    private static Field[] getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentType = type;
        while (currentType != null && currentType != Object.class) {
            fields.addAll(Arrays.asList(currentType.getDeclaredFields()));
            currentType = currentType.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    private static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class
                || type == Integer.class
                || type == double.class
                || type == Double.class
                || type == long.class
                || type == Long.class
                || type == float.class
                || type == Float.class
                || type == boolean.class
                || type == Boolean.class
                || type == short.class
                || type == Short.class
                || type == byte.class
                || type == Byte.class
                || type == char.class
                || type == Character.class
                || type == Date.class
                || type == LocalDate.class;
    }

    private static Object convertValue(String value,Class<?> type ) {
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
            if (value.length() != 1) {
                throw new IllegalArgumentException("Caractère invalide : " + value);
            }
            return value.charAt(0);
        }
        if (type == Date.class) {
            return Date.valueOf(value);
        }
        if (type == LocalDate.class) {
            return LocalDate.parse(value);
        }
        throw new IllegalArgumentException(
                "Type non supporté : " + type.getName()
        );
    }

    private static Mapfile bindFile(HttpServletRequest request, String name) throws Exception {
        Part part = request.getPart(name);
        if (part == null || part.getSize() == 0) {
            return null;
        }
        Mapfile mapfile = creerMapfile(part);
        sauvegarderFichier(request, mapfile);
        return mapfile;
    }

    private static boolean isListOfMapfile(Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) generic).getActualTypeArguments();
            return args.length == 1 && args[0] == Mapfile.class;
        }
        return false;
    }

    private static List<Mapfile> bindFiles(HttpServletRequest request, String name) throws Exception {
        List<Mapfile> result = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (!name.equals(part.getName()) || part.getSize() == 0) {
                continue;
            }
            Mapfile mapfile = creerMapfile(part);
            sauvegarderFichier(request, mapfile);
            result.add(mapfile);
        }
        return result;
    }

    private static Mapfile creerMapfile(Part part) throws IOException {
        Mapfile mapfile = new Mapfile();
        String fileName = part.getSubmittedFileName();
        mapfile.setNom(fileName);

        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            mapfile.setExtension(fileName.substring(index));
        }
        try (InputStream input = part.getInputStream()) {
            mapfile.setData(input.readAllBytes());
        }
        return mapfile;
    }

    private static synchronized void sauvegarderFichier(HttpServletRequest request,Mapfile mapfile) throws Exception {
        ServletContext ctx = request.getServletContext();

        // 1. Trouver uploads.properties
        String propsPath = ctx.getInitParameter("uploads.config");
        if (propsPath == null) {
            propsPath = System.getProperty("uploads.config");
        }
        if (propsPath == null) {
            throw new IllegalStateException("Chemin de uploads.properties non défini");
        }

        File propsFile = new File(propsPath);
        Properties props = new Properties();
        if (propsFile.exists()) {
            try (InputStream in = new FileInputStream(propsFile)) {
                props.load(in);
            }
        }

        // 2. Dossier des fichiers de bytes = dossier de "fichier.bytes", sinon uploads/
        String cheminRef = props.getProperty("fichier.bytes");
        File dossier;
        if (cheminRef == null || cheminRef.trim().isEmpty()) {
            dossier = new File(propsFile.getParentFile(), "uploads");
        } else {
            File ref = new File(cheminRef);
            if (!ref.isAbsolute()) {
                ref = new File(propsFile.getParentFile(), cheminRef);
            }
            dossier = ref.getParentFile();
        }
        dossier.mkdirs();

        // 3. Un fichier .bytes par image, avec un nom unique
        String id = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        File fichierBytes = new File(dossier, id + ".bytes");
        Files.write(fichierBytes.toPath(), mapfile.getData());

        // 4. Une clé par fichier dans le .properties
        props.setProperty("fichier.bytes." + id, fichierBytes.getAbsolutePath());
        try (OutputStream out = new FileOutputStream(propsFile)) {
            props.store(out, "Fichiers contenant les bytes");
        }

        System.out.println("Bytes sauvegardés dans : " + fichierBytes.getAbsolutePath());
    }
}
