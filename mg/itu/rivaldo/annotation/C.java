package mg.itu.rivaldo.annotation;

@Url("/home")
public class C {
    public void home() {
        System.out.println("Welcome to the Home Page!");
    }

    public void about() {
        System.out.println("This is the About Page.");
    }
}
