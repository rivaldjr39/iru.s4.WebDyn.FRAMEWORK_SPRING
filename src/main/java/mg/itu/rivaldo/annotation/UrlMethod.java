package mg.itu.rivaldo.annotation;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMethod {
    String value();
    String type() default "GET";
}
