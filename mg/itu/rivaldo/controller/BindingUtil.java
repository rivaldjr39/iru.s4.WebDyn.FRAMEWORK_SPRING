package mg.itu.rivaldo.controller;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.rivaldo.annotation.Param;
import java.lang.reflect.Field;
import java.sql.Date;

public class BindingUtil {

    public static Object bindObject(Class<?> objectType,HttpServletRequest request) throws Exception {

        Object obj = objectType.getDeclaredConstructor().newInstance();
        Field[] fields = objectType.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(Param.class)) {
                continue;
            }

            Param param = field.getAnnotation(Param.class);
            String inputName = param.value();
            
            String parameterValue = request.getParameter(inputName);
            Class<?> fieldType = field.getType();

            if (isSimpleType(fieldType)) {
                if (parameterValue == null) {
                    continue;
                }
                Object convertedValue = convertValue(parameterValue,fieldType);
                field.setAccessible(true);
                field.set(obj, convertedValue);
            }else {

                Object childObject = bindObject(fieldType,request);
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
                || type == Date.class;
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
        throw new IllegalArgumentException(
                "Type non supporté : " + type.getName()
        );
    }
}