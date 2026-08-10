package tech.kayys.wayang.session;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Optional;

import tech.kayys.wayang.extension.Principal;
import tech.kayys.wayang.extension.Extension;

/**
 * Session Provider - manages sessions.
 */
public interface SessionProvider extends Extension {
    
    /**
     * Create session
     */
    AgentSession createSession(Principal principal) throws Exception;
    
    /**
     * Get session
     */
    Optional<AgentSession> getSession(String sessionId) throws Exception;
    
    /**
     * Save session
     */
    void saveSession(AgentSession session) throws Exception;
    
    /**
     * Delete session
     */
    void deleteSession(String sessionId) throws Exception;
    
    /**
     * Get all sessions for user
     */
    List<AgentSession> getSessionsForUser(String userId) throws Exception;
    
    /**
     * Invalidate session
     */
    default void invalidateSession(String sessionId) throws Exception {
        deleteSession(sessionId);
    }
    
    /**
     * Check if session is active
     */
    default boolean isActive(String sessionId) throws Exception {
        return getSession(sessionId).map(s -> s.status() == SessionStatus.ACTIVE).orElse(false);
    }
}
