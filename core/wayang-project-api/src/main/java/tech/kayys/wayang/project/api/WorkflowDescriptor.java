package tech.kayys.wayang.project.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor for a workflow within a Wayang project.
 */
public class WorkflowDescriptor {
    
    private String name;
    private String description;
    private String type;
    private List<NodeDescriptor> nodes;
    private List<ConnectionDescriptor> connections;
    private Map<String, Object> metadata;
    
    public WorkflowDescriptor() {
        this.nodes = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.type = "agent-workflow";
    }
    
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<NodeDescriptor> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<NodeDescriptor> nodes) {
        this.nodes = nodes;
    }
    
    public List<ConnectionDescriptor> getConnections() {
        return connections;
    }
    
    public void setConnections(List<ConnectionDescriptor> connections) {
        this.connections = connections;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
