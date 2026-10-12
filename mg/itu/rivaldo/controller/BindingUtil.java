package mg.itu.rivaldo.controller;

import jakarta.servlet.http.HttpServletRequest;

<<<<<<< Updated upstream
import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import jakarta.servlet.http.Part;
import mg.itu.rivaldo.annotation.Mapfile;

import java.io.InputStream;

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
=======
import java.lang.reflect.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class BindingUtil {

    // Point d'entrée : créer et remplir un objet
    public static Object bindObject(Class<?> objectType,
                                    HttpServletRequest request) throws Exception {
        Object obj = objectType.getDeclaredConstructor().newInstance();
        bindFields(obj, objectType, request, "");
        return obj;
>>>>>>> Stashed changes
    }

    // Parcourir tous les attributs de l'objet
    private static void bindFields(Object obj, Class<?> type,
                                   HttpServletRequest request,
                                   String prefix) throws Exception {

        for (Field field : getAllFields(type)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            Class<?> fieldType = field.getType();
            String name = prefix.isEmpty()
                    ? field.getName()
                    : prefix + "." + field.getName();

            if (isSimpleType(fieldType)) {
                String value = request.getParameter(name);

                if (value != null && !value.isEmpty()) {
                    field.set(obj, convertValue(value, fieldType));
                }
            } else if (Collection.class.isAssignableFrom(fieldType)) {
                bindList(obj, field, request, name);
            } else if (fieldType.isArray()) {
                bindArray(obj, field, request, name);
            } else if (!fieldType.isEnum() && !Map.class.isAssignableFrom(fieldType)) {
                bindNestedObject(obj, field, request, name);
            }
        }
    }

<<<<<<< Updated upstream
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

    private static Mapfile bindFile(
        HttpServletRequest request,
        String name
) throws Exception {

    System.out.println("=== DEBUT BIND FILE ===");
    System.out.println("Nom du champ : " + name);
    System.out.println("Content-Type : " + request.getContentType());

    Part part = request.getPart(name);

    if (part == null) {
        System.out.println("ERREUR : part est null !");
        return null;
    }

    System.out.println("Nom du fichier : " + part.getSubmittedFileName());
    System.out.println("Taille du fichier : " + part.getSize());

    if (part.getSize() == 0) {
        System.out.println("ERREUR : fichier vide !");
        return null;
    }

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

    System.out.println("Fichier récupéré : " + mapfile.getNom());

    return mapfile;
}
}

=======
    // Gérer les objets imbriqués : personne.nom
    private static void bindNestedObject(Object obj, Field field,
                                         HttpServletRequest request,
                                         String name) throws Exception {

        boolean exists = request.getParameterMap().keySet().stream()
                .anyMatch(key -> key.startsWith(name + "."));

        if (!exists) {
            return;
        }

        Object child = field.getType().getDeclaredConstructor().newInstance();

        bindFields(child, field.getType(), request, name);
        field.set(obj, child);
    }

    // Gérer les listes : personnes[0].nom
    private static void bindList(Object obj, Field field,
                                 HttpServletRequest request,
                                 String name) throws Exception {

        Type genericType = field.getGenericType();

        if (!(genericType instanceof ParameterizedType)) {
            return;
        }

        Type elementType = ((ParameterizedType) genericType)
                .getActualTypeArguments()[0];

        Class<?> elementClass = getClassType(elementType);

        if (elementClass == null) {
            return;
        }

        List<Object> list = new ArrayList<>();

        for (int i : getIndexes(request, name)) {
            String itemName = name + "[" + i + "]";

            if (isSimpleType(elementClass)) {
                String value = request.getParameter(itemName);

                if (value != null) {
                    list.add(convertValue(value, elementClass));
                }
            } else {
                Object item = elementClass.getDeclaredConstructor().newInstance();
                bindFields(item, elementClass, request, itemName);
                list.add(item);
            }
        }

        // ArrayList est compatible avec List et la plupart des interfaces de collection.
        if (field.getType().isAssignableFrom(ArrayList.class)) {
            field.set(obj, list);
        }
    }

    // Gérer les tableaux : personnes[0].nom
    private static void bindArray(Object obj, Field field,
                                  HttpServletRequest request,
                                  String name) throws Exception {

        Class<?> elementClass = field.getType().getComponentType();
        List<Object> values = new ArrayList<>();

        for (int i : getIndexes(request, name)) {
            String itemName = name + "[" + i + "]";

            if (isSimpleType(elementClass)) {
                String value = request.getParameter(itemName);

                if (value != null) {
                    values.add(convertValue(value, elementClass));
                }
            } else {
                Object item = elementClass.getDeclaredConstructor().newInstance();
                bindFields(item, elementClass, request, itemName);
                values.add(item);
            }
        }

        Object array = Array.newInstance(elementClass, values.size());

        for (int i = 0; i < values.size(); i++) {
            Array.set(array, i, values.get(i));
        }

        field.set(obj, array);
    }

    // Récupérer les indices présents dans les paramètres
    private static Set<Integer> getIndexes(HttpServletRequest request,String name) {
        Set<Integer> indexes = new TreeSet<>();

        for (String key : request.getParameterMap().keySet()) {
            if (key.startsWith(name + "[")) {
                int start = name.length() + 1;
                int end = key.indexOf("]", start);

                if (end > start) {
                    try {
                        indexes.add(Integer.parseInt(key.substring(start, end)));
                    } catch (NumberFormatException ignored) {
                        // Ignorer les indices invalides
                    }
                }
            }
        }

        return indexes;
    }

    // Récupérer la classe d'un type
    private static Class<?> getClassType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }

        return null;
    }

    // Récupérer les attributs de la classe et de ses parents
    private static Field[] getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }

        return fields.toArray(new Field[0]);
    }

    // Identifier les types simples
    private static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == double.class || type == Double.class
                || type == long.class || type == Long.class
                || type == float.class || type == Float.class
                || type == boolean.class || type == Boolean.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class
                || type == char.class || type == Character.class
                || type == Date.class || type == LocalDate.class;
    }

    // Convertir une chaîne vers le type Java demandé
    private static Object convertValue(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == float.class || type == Float.class) return Float.parseFloat(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == short.class || type == Short.class) return Short.parseShort(value);
        if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
        if (type == char.class || type == Character.class) return value.charAt(0);
        if (type == Date.class) return Date.valueOf(value);
        if (type == LocalDate.class) return LocalDate.parse(value);

        throw new IllegalArgumentException("Type non supporté : " + type.getName());
    }
}
>>>>>>> Stashed changes
