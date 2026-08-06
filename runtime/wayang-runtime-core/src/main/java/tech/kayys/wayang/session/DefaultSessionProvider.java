package tech.kayys.wayang.session;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultSessionProvider implements SessionProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, AgentSession> sessions = new ConcurrentHashMap<>();
    
    public DefaultSessionProvider() {
        this.id = Id.random().asString();
        this.name = "default-session-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Session Provider")
            .version(version)
            .label("type", "session")
            .now()
            .build();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("session"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public AgentSession createSession(Principal principal) throws Exception {
        AgentSession session = AgentSession.of(principal.id().asString());
        sessions.put(session.id(), session);
        return session;
    }
    
    @Override
    public Optional<AgentSession> getSession(String sessionId) throws Exception {
        AgentSession session = sessions.get(sessionId);
        if (session != null && session.status() == SessionStatus.ACTIVE) {
            // Check expiration
            if (session.expiresAt().isBefore(Instant.now())) {
                session = session.withStatus(SessionStatus.EXPIRED);
                sessions.put(sessionId, session);
                return Optional.empty();
            }
            // Touch the session
            session = session.touch();
            sessions.put(sessionId, session);
            return Optional.of(session);
        }
        return Optional.empty();
    }
    
    @Override
    public void saveSession(AgentSession session) throws Exception {
        sessions.put(session.id(), session);
    }
    
    @Override
    public void deleteSession(String sessionId) throws Exception {
        sessions.remove(sessionId);
    }
    
    @Override
    public List<AgentSession> getSessionsForUser(String userId) throws Exception {
        return sessions.values().stream()
            .filter(s -> s.userId().equals(userId))
            .collect(Collectors.toList());
    }
}
