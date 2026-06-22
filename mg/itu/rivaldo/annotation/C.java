package mg.itu.rivaldo.annotation;

@Url("/home")
public class C {
    @UrlMethod("/home")
    public void home() {
        System.out.println("Welcome to the Home Page!");
    }

    @UrlMethod("/about")
    public void about() {
        System.out.println("This is the About Page.");
    }
}
