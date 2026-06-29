package mg.itu.rivaldo.annotation;

@Url("/home")
public class C {
    @UrlMethod(value = "/bienvenue", type = "GET")
    public void home() {
        System.out.println("la methode home() est appelé");
    }

    @UrlMethod(value = "/test", type = "GET")
    public void test() {
        System.out.println("la methode test() est appelé");
    }
}
