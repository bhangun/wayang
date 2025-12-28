package tech.kayys.wayang.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.kayys.wayang.schema.workflow.WorkflowDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow Design Request.
 * 
 * Used for creating and updating workflow definitions.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDesignRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private WorkflowDefinition definition;

    private List<String> tags;
    private Map<String, Object> metadata;
    private String workspaceId;

    public WorkflowDesignRequest() {
        this.tags = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkflowDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(WorkflowDefinition definition) {
        this.definition = definition;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
