package mg.itu.rivaldo.annotation;

@Url("/home")
public class C {
    @UrlMethod("/bienvenue")
    public void home() {
    }

    @UrlMethod("/test")
    public void test() {
    }
}
