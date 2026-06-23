package mg.itu.rivaldo.annotation;

@Url("/home")
public class C {
    @UrlMethod(value = "/bienvenue", type = "GET")
    public void home() {
    }

    @UrlMethod(value = "/test", type = "GET")
    public void test() {
    }
}
