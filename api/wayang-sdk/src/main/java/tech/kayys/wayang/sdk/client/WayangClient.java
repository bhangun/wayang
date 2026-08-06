package tech.kayys.wayang.sdk.client;

import tech.kayys.wayang.sdk.agent.WayangAgent;
import tech.kayys.wayang.sdk.gollek.ProjectStore;

import java.nio.file.Path;

/**
 * Public SDK entry point that groups Wayang's stable product APIs by concern.
 */
public final class WayangClient implements AutoCloseable {

    private final WayangAgent agent;
    private final ProjectStore projectStore;

    private final WayangProviderApi providers;
    private final WayangModelApi models;
    private final WayangInferenceApi inference;
    private final WayangProjectApi projects;
    private final WayangSessionApi sessions;

    private WayangClient(WayangAgent agent, ProjectStore projectStore) {
        this.agent = agent;
        this.projectStore = projectStore;

        this.providers = new WayangProviderApi();
        this.models = new WayangModelApi();
        this.inference = new WayangInferenceApi(agent);
        this.projects = new WayangProjectApi(projectStore);
        this.sessions = new WayangSessionApi(projectStore);
    }

    public static WayangClient create(WayangAgent agent, Path workspaceDir) {
        ProjectStore store;
        try {
            store = new ProjectStore(workspaceDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ProjectStore", e);
        }
        return new WayangClient(agent, store);
    }

    public WayangAgent getAgent() {
        return agent;
    }

    public WayangProviderApi providers() {
        return providers;
    }

    public WayangModelApi models() {
        return models;
    }

    public WayangInferenceApi inference() {
        return inference;
    }

    public WayangProjectApi projects() {
        return projects;
    }

    public WayangSessionApi sessions() {
        return sessions;
    }

    @Override
    public void close() {
        // Implement shutdown logic if needed
    }
}
