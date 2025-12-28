package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * 
 * Workflow event request for external event injection.
 */
public class WorkflowEventRequest {
    private String eventType;
    private String correlationKey;
    private Map<String, Object> payload;
    private Instant timestamp;

    // Getters and setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
