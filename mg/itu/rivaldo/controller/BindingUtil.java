package mg.itu.rivaldo.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDate;

public class BindingUtil {

    public static Object bindObject(Class<?> objectType, HttpServletRequest request) throws Exception {

        // Création de l'objet
        Object obj = objectType.getDeclaredConstructor().newInstance();

        // Parcours des champs de la classe
        Field[] fields = objectType.getDeclaredFields();

        for (Field field : fields) {

            Class<?> fieldType = field.getType();

            // Récupération du paramètre avec le nom du champ
            if (isSimpleType(fieldType)) {

                String parameterValue = request.getParameter(field.getName());

                // Si le paramètre n'existe pas, on conserve la valeur par défaut
                if (parameterValue == null || parameterValue.isEmpty()) {
                    continue;
                }

                Object convertedValue = convertValue(parameterValue, fieldType);
                field.setAccessible(true);
                field.set(obj, convertedValue);
            }

            // Gestion des objets imbriqués
            else if (!fieldType.isArray() && !java.util.Collection.class.isAssignableFrom(fieldType) && !java.util.Map.class.isAssignableFrom(fieldType)) {

                Object childObject = bindObject(fieldType, request);
                field.setAccessible(true);
                field.set(obj, childObject);
            }
        }

        return obj;
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

    private static Object convertValue(String value,Class<?> type) {

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
}