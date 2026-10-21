package mg.itu.rivaldo.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapSession {
    private Map<String, Object> attributes;

    public MapSession() {
        this.attributes = new HashMap<>();
    }

    public MapSession(Map<String, Object> attributes) {
        this.attributes = (attributes != null) ? attributes : new HashMap<>();
    }

    public void add(String key, Object value) {
        attributes.put(key, value);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public void remove(String key) {
        attributes.remove(key);
    }

    public boolean contains(String key) {
        return attributes.containsKey(key);
    }

    public void clear() {
        attributes.clear();
    }

    public Set<String> getKeys() {
        return attributes.keySet();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = (attributes != null) ? attributes : new HashMap<>();
    }
}