package mg.itu.rivaldo.controller;

public class UrlType {
    private String url;
    private String verb;

    public UrlType(String url, String verb) {
        this.url = url;
        this.verb = verb;
    }

    public String getUrl() {
        return url;
    }

    public String getVerb() {
        return verb;
    }


    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
            
        UrlType urlType = (UrlType) obj;
        if (!url.equals(urlType.url)){
            return false;
        }
        return verb.equals(urlType.verb);
    }
}
