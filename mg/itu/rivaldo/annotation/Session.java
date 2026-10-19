package mg.itu.rivaldo.annotation;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private Map<String, Object> sessionData;

    public Session() {
        this.sessionData = new HashMap<>();
    }

    public Session(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }
     
    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    public void clear() {
        sessionData.clear();
    }

    public Map<String, Object> getSessionData() {
        return sessionData;
    }
    public void setSessionData(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }
}