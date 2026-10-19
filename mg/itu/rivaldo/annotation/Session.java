package mg.itu.rivaldo.annotation;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
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

    public void loadFrom(HttpSession httpSession) {
        sessionData.clear();
        for (String name : Collections.list(httpSession.getAttributeNames())) {
            sessionData.put(name, httpSession.getAttribute(name));
        }
    }

    public void applyTo(HttpSession httpSession) {
        synchronized (httpSession) {
            for (String name : Collections.list(httpSession.getAttributeNames())) {
                if (!sessionData.containsKey(name)) {
                    httpSession.removeAttribute(name);
                }
            }
            for (Map.Entry<String, Object> entry : sessionData.entrySet()) {
                httpSession.setAttribute(entry.getKey(), entry.getValue());
            }
        }
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