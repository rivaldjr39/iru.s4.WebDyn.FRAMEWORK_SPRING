package mg.itu.rivaldo.annotation;

@Url("/home")
public class C {
    @UrlMethod("/home")
    public void home() {
    }

    @UrlMethod("/about")
    public void about() {
    }
}
