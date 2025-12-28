package tech.kayys.wayang.node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Node Suggestion Request.
 * 
 * Provides context for intelligent node type suggestions.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeSuggestionRequest {

    private String currentNodeId;
    private String currentNodeType;
    private String outputPort;
    private String outputType;
    private Map<String, Object> workflowContext;

    public NodeSuggestionRequest() {
        this.workflowContext = new HashMap<>();
    }

    // Getters and setters
    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getCurrentNodeType() {
        return currentNodeType;
    }

    public void setCurrentNodeType(String currentNodeType) {
        this.currentNodeType = currentNodeType;
    }

    public String getOutputPort() {
        return outputPort;
    }

    public void setOutputPort(String outputPort) {
        this.outputPort = outputPort;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public Map<String, Object> getWorkflowContext() {
        return workflowContext;
    }

    public void setWorkflowContext(Map<String, Object> workflowContext) {
        this.workflowContext = workflowContext;
    }
}
