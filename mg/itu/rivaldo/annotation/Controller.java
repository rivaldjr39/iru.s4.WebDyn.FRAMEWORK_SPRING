package mg.itu.rivaldo.annotation;

@Url("/home")
public class Controller {
    @UrlMethod(value = "/home", type = "GET")
    public void hometypeget() {}


    @UrlMethod(value = "/home", type = "POST")
    public void hometypepost() {}
}