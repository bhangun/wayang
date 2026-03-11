package tech.kayys.wayang.project.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Descriptor for a node within a workflow.
 */
public class NodeDescriptor {
    
    private String id;
    private String type;
    private String executor;
    private String description;
    private Map<String, Object> config;
    
    public NodeDescriptor() {
        this.config = new HashMap<>();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getExecutor() {
        return executor;
    }
    
    public void setExecutor(String executor) {
        this.executor = executor;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getConfig() {
        return config;
    }
    
    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
