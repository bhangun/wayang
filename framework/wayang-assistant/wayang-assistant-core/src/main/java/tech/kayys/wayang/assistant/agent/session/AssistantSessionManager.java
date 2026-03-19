package tech.kayys.wayang.assistant.agent.session;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.kayys.wayang.assistant.agent.ConversationSession;
import tech.kayys.wayang.assistant.agent.ConversationSession.ConversationMessage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conversation sessions and their history.
 */
@ApplicationScoped
public class AssistantSessionManager {

    @ConfigProperty(name = "wayang.assistant.session.max-sessions", defaultValue = "500")
    int maxSessions = 500;

    private final ConcurrentHashMap<String, ConversationSession> sessions = new ConcurrentHashMap<>();

    public ConversationSession getOrCreateSession(String sessionId) {
        // Evict oldest sessions if we hit the cap (simple LRU-ish eviction)
        if (sessions.size() >= maxSessions) {
            sessions.entrySet().stream()
                    .min(Comparator.comparing(e -> e.getValue().getLastAccessedAt()))
                    .ifPresent(e -> sessions.remove(e.getKey()));
        }
        return sessions.computeIfAbsent(sessionId, ConversationSession::new);
    }

    public List<ConversationMessage> getSessionHistory(String sessionId) {
        ConversationSession session = sessions.get(sessionId);
        return session == null ? Collections.emptyList() : session.getHistory();
    }

    public boolean deleteSession(String sessionId) {
        return sessions.remove(sessionId) != null;
    }

    public int activeSessionCount() {
        return sessions.size();
    }
    
    public ConcurrentHashMap<String, ConversationSession> getSessions() {
        return sessions;
    }
}
