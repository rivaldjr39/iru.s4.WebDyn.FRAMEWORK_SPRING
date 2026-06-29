package mg.itu.rivaldo.annotation;

@Url("/home")
public class Controller {
    @UrlMethod(value = "/home", type = "GET")
    public void hometypeget() {
        System.out.println("la methode hometypeget() est appelé");
    }


    @UrlMethod(value = "/home", type = "POST")
    public void hometypepost() {
        System.out.println("la methode hometypepost() est appelé");
    }
}