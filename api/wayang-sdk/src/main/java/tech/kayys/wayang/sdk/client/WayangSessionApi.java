package tech.kayys.wayang.sdk.client;

import tech.kayys.wayang.sdk.gollek.ProjectStore;
import tech.kayys.wayang.sdk.gollek.model.Session;
import java.util.List;

/**
 * API for managing Wayang sessions.
 */
public final class WayangSessionApi {

    private final ProjectStore projectStore;

    public WayangSessionApi(ProjectStore projectStore) {
        this.projectStore = projectStore;
    }

    public Session createSession(String projectId, String name) {
        try {
            return projectStore.createSession(projectId, name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create session", e);
        }
    }

    public void updateSession(String projectId, Session session) {
        try {
            projectStore.updateSession(projectId, session);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update session", e);
        }
    }

    public List<String> listSessions(String projectId) {
        try {
            return projectStore.listSessions(projectId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to list sessions", e);
        }
    }

    public boolean deleteSession(String projectId, String sessionId) {
        try {
            return projectStore.deleteSession(projectId, sessionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete session", e);
        }
    }

    public Session cloneSession(String projectId, String sessionId, String newName, Integer limit) {
        try {
            return projectStore.cloneSession(projectId, sessionId, newName, limit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone session", e);
        }
    }
}
