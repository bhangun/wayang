package tech.kayys.wayang.project.api;

/**
 * Descriptor for a connection between nodes in a workflow.
 */
public class ConnectionDescriptor {
    
    private String from;
    private String to;
    private String condition;
    
    public ConnectionDescriptor() {
    }
    
    public ConnectionDescriptor(String from, String to) {
        this.from = from;
        this.to = to;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
