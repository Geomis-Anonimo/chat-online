package prosistemas.support.chat.websocket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component 
public class OnlineAdmins {
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public void add(String sessionId, Long adminId) {
        sessions.put(sessionId, adminId);
    }

    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    public Set<Long> ids() {
        return new HashSet<>(sessions.values());
    }
}
