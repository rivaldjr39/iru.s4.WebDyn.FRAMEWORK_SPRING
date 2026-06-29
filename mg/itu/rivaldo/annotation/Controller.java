package mg.itu.rivaldo.annotation;
import mg.itu.rivaldo.annotation.Url;
import mg.itu.rivaldo.annotation.UrlMethod;

@Url("/home")
public class Controller {
    @UrlMethod(value = "/home", type = "GET")
    public void home() {}

    @UrlMethod(value = "/home", type = "POST")
    public void about() {}
}