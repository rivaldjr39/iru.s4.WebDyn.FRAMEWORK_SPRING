package mg.itu.rivaldo.annotation;
import mg.itu.rivaldo.annotation.Url;
import mg.itu.rivaldo.annotation.UrlMethod;

@Url("/home")
public class Controller {
    @UrlMethod("/home")
    public void home() {
        System.out.println("Welcome to the Home Page!");
    }
    @UrlMethod("/home")
    public void about() {
        System.out.println("This is the About Page.");
    }
}