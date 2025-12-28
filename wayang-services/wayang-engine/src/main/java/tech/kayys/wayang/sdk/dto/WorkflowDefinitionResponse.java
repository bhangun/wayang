package tech.kayys.wayang.sdk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tech.kayys.wayang.schema.workflow.WorkflowDefinition;
import java.util.List;
import java.util.Map;

/**
 * SDK DTO for Workflow Definition Response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDefinitionResponse {

    private String id;
    private String name;
    private String version;
    private String description;
    private String status;
    private WorkflowDefinition definition;
    private List<String> tags;
    private Map<String, Object> metadata;
    private String createdAt;
    private String updatedAt;

    public WorkflowDefinitionResponse() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
