package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.*;

/**
 * Response for workflow execution events during streaming.
 */
public class WorkflowExecutionEvent {
    private String eventId;
    private String runId;
    private String eventType; // NODE_STARTED, NODE_COMPLETED, NODE_FAILED, WORKFLOW_COMPLETED, etc.
    private String nodeId;
    private Instant timestamp;
    private Map<String, Object> data;
    private String status;

    // Getters and setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static WorkflowExecutionEvent error(String message) {
        WorkflowExecutionEvent event = new WorkflowExecutionEvent();
        event.setEventType("ERROR");
        event.setStatus("FAILED");
        event.setTimestamp(Instant.now());
        event.setData(Map.of("error", message));
        return event;
    }
}
