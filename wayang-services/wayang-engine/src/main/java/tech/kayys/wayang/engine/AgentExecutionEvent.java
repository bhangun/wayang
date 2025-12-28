package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Agent execution event for streaming agentic workflows.
 */
public class AgentExecutionEvent {
    private String eventId;
    private String runId;
    private String agentId;
    private String eventType; // REASONING, TOOL_CALL, DECISION, COMPLETION, ERROR
    private String reasoning;
    private String action;
    private Map<String, Object> toolCall;
    private Object result;
    private Double confidence;
    private Instant timestamp;

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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getToolCall() {
        return toolCall;
    }

    public void setToolCall(Map<String, Object> toolCall) {
        this.toolCall = toolCall;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static AgentExecutionEvent error(String message) {
        AgentExecutionEvent event = new AgentExecutionEvent();
        event.setEventType("ERROR");
        event.setTimestamp(Instant.now());
        event.setReasoning(message);
        return event;
    }
}
