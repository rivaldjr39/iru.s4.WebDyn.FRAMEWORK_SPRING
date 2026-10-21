package mg.itu.rivaldo.util;

import java.util.ArrayList;
import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import mg.itu.rivaldo.annotation.MapSession;

public final class SessionUtil {
    private static final String MAPSESSION_KEY = "mapsession";

    private SessionUtil() {}

    /** Copie les attributs de la HttpSession dans le MapSession donné. */
    public static MapSession initMapSession(HttpServletRequest request, MapSession mapSession) {
        if (mapSession == null) {
            mapSession = new MapSession();
        }

        HttpSession session = request.getSession();
        for (String name : Collections.list(session.getAttributeNames())) {
            mapSession.add(name, session.getAttribute(name));
        }

        setMapSessionContext(request, mapSession);
        return mapSession;
    }

    /** Répercute le MapSession sur la HttpSession (ajouts et modifications). */
    public static void rectifyHttpSession(HttpServletRequest request, MapSession mapSession) {
        if (mapSession == null) {
            return;
        }

        HttpSession session = request.getSession();
        for (String name : new ArrayList<>(mapSession.getKeys())) {
            session.setAttribute(name, mapSession.get(name));
        }
    }


    /** Stocké au niveau de la requête (et non du ServletContext) pour ne pas mélanger les utilisateurs. */
    public static void setMapSessionContext(HttpServletRequest request, MapSession mapSession) {
        request.setAttribute(MAPSESSION_KEY, mapSession);
    }

    public static MapSession getMapSessionContext(HttpServletRequest request) {
        return (MapSession) request.getAttribute(MAPSESSION_KEY);
    }

    public static MapSession fromHttpSession(HttpServletRequest request) {
        return initMapSession(request, new MapSession());
    }

    /** Synchronise complètement : supprime de la HttpSession ce qui n'est plus dans le MapSession. */
    public static void syncWithHttpSession(HttpServletRequest request, MapSession mapSession) {
        if (mapSession == null) {
            return;
        }

        HttpSession session = request.getSession();
        for (String name : Collections.list(session.getAttributeNames())) {
            if (!mapSession.getAttributes().containsKey(name)) {
                session.removeAttribute(name);
            }
        }

        rectifyHttpSession(request, mapSession);
    }
}
