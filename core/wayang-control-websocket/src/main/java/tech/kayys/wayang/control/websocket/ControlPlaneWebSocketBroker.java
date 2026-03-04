package tech.kayys.wayang.control.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

@ApplicationScoped
public class ControlPlaneWebSocketBroker {

    private final ConcurrentHashMap<String, Set<Session>> rooms = new ConcurrentHashMap<>();

    public void join(String workspaceId, Session session) {
        rooms.computeIfAbsent(workspaceId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void leave(String workspaceId, Session session) {
        Set<Session> sessions = rooms.get(workspaceId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            rooms.remove(workspaceId);
        }
    }

    public int roomSize(String workspaceId) {
        Set<Session> sessions = rooms.get(workspaceId);
        return sessions == null ? 0 : sessions.size();
    }

    public void send(Session session, String payload) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        session.getBasicRemote().sendText(payload);
    }

    public void broadcast(String workspaceId, String payload, Session excludedSession) {
        for (Session session : roomSessions(workspaceId)) {
            if (session == excludedSession) {
                continue;
            }
            if (!session.isOpen()) {
                continue;
            }
            session.getAsyncRemote().sendText(payload);
        }
    }

    private Set<Session> roomSessions(String workspaceId) {
        return rooms.getOrDefault(workspaceId, Collections.emptySet());
    }
}
