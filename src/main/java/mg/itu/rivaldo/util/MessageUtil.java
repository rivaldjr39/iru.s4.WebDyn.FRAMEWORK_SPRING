package mg.itu.rivaldo.util;

import java.util.HashMap;
import java.util.Map;

public final class MessageUtil {

    
    private static final String DEFAULT_LANG = "en";
    private static volatile String lang = DEFAULT_LANG;

    
    private static final Map<String, Map<String, String>> MESSAGES = new HashMap<>();

    static {
        Map<String, String> fr = new HashMap<>();
        fr.put("no.mapping", "Aucune correspondance trouvée pour l'URL : %s");
        fr.put("error.request", "Erreur lors du traitement de la requête : %s");
        fr.put("auth.required", "Authentification requise");
        fr.put("access.denied", "Accès refusé : l'utilisateur n'a pas le rôle requis.");

        Map<String, String> en = new HashMap<>();
        en.put("no.mapping", "No match found for URL: %s");
        en.put("error.request", "Error while processing the request: %s");
        en.put("auth.required", "Authentication required");
        en.put("access.denied", "Access denied: user does not have the required role.");

        MESSAGES.put("fr", fr);
        MESSAGES.put("en", en);
    }

    private MessageUtil() {}

    public static void setLang(String newLang) {
        if (newLang != null && !newLang.isEmpty() && MESSAGES.containsKey(newLang)) {
            lang = newLang;
        } else {
            lang = DEFAULT_LANG;
        }
    }

    public static String getLang() {
        return lang;
    }

    public static String get(String key, Object... args) {
        String message = MESSAGES.get(lang).get(key);

        if (message == null) {
            message = MESSAGES.get(DEFAULT_LANG).getOrDefault(key, key);
        }
        return args.length == 0 ? message : String.format(message, args);
    }
}