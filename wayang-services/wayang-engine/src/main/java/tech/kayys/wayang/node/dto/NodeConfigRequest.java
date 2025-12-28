package tech.kayys.wayang.node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Node Configuration Validation Request.
 * 
 * Used to validate node configuration against node type schema.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeConfigRequest {

    @NotNull
    private String nodeTypeId;

    @NotNull
    private Map<String, Object> config;

    public NodeConfigRequest() {
        this.config = new HashMap<>();
    }

    public NodeConfigRequest(String nodeTypeId, Map<String, Object> config) {
        this.nodeTypeId = nodeTypeId;
        this.config = config;
    }

    // Getters and setters
    public String getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
