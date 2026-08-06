package tech.kayys.wayang.sdk.client;

import tech.kayys.wayang.sdk.gollek.ProjectStore;
import tech.kayys.wayang.sdk.gollek.model.Project;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * API for managing Wayang workspaces and projects.
 */
public final class WayangProjectApi {

    private final ProjectStore projectStore;

    public WayangProjectApi(ProjectStore projectStore) {
        this.projectStore = projectStore;
    }

    public Project createProject(String id, String name, String directory) {
        try {
            return projectStore.createProject(id, name, directory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create project", e);
        }
    }

    public void removeProject(String id) {
        try {
            projectStore.removeProject(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove project", e);
        }
    }

    public void renameProject(String id, String newName) {
        try {
            projectStore.renameProject(id, newName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rename project", e);
        }
    }

    public List<Project> listProjects() {
        try {
            return projectStore.listProjects();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list projects", e);
        }
    }

    public void switchProject(String id) {
        try {
            projectStore.switchProject(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to switch project", e);
        }
    }

    public Path exportProject(String id, Path output) {
        try {
            return projectStore.exportProject(id, output);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export project", e);
        }
    }

    public Project importProject(Path archive) {
        try {
            return projectStore.importProject(archive);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import project", e);
        }
    }

    public Optional<String> currentProject() {
        try {
            return Optional.ofNullable(projectStore.currentProject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get current project", e);
        }
    }
}
