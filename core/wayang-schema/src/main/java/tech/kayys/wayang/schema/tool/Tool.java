package tech.kayys.wayang.schema.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.kayys.wayang.schema.common.Metadata;
import java.util.List;
import java.util.Map;

/**
 * Represents a tool that can be used by agents in the system.
 */
public class Tool {
    @JsonProperty("metadata")
    private Metadata metadata;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    @JsonProperty("requiredParameters")
    private List<String> requiredParameters;

    @JsonProperty("connectionId")
    private String connectionId;

    @JsonProperty("configuration")
    private Map<String, Object> configuration;

    public Tool() {
        // Default constructor for JSON deserialization
    }

    public Tool(Metadata metadata, String type, String description, Map<String, Object> parameters,
                List<String> requiredParameters, String connectionId, Map<String, Object> configuration) {
        this.metadata = metadata;
        this.type = type;
        this.description = description;
        this.parameters = parameters;
        this.requiredParameters = requiredParameters;
        this.connectionId = connectionId;
        this.configuration = configuration;
    }

    // Getters
    public Metadata getMetadata() {
        return metadata;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public List<String> getRequiredParameters() {
        return requiredParameters;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    // Setters
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setRequiredParameters(List<String> requiredParameters) {
        this.requiredParameters = requiredParameters;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}