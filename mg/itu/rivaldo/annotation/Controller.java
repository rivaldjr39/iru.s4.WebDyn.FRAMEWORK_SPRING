package mg.itu.rivaldo.annotation;
import mg.itu.rivaldo.annotation.Url;

@Url("/home")
public class Controller {
    public void home() {
        System.out.println("Welcome to the Home Page!");
    }

    public void about() {
        System.out.println("This is the About Page.");
    }
}