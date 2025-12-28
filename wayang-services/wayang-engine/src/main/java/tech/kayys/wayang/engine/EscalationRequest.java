package tech.kayys.wayang.engine;

import java.util.Map;

/**
 * 
 * Escalation request.
 */
public class EscalationRequest {
    private String reason;
    private String assignTo;
    private String priority = "HIGH";
    private Map<String, Object> context;

    // Getters and setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}
