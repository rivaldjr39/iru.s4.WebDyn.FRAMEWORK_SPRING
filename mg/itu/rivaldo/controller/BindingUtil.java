package mg.itu.rivaldo.controller;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collection;

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

            // 1. Attribut de type simple
            if (isSimpleType(fieldType)) {
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
}