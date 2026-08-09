package tech.kayys.wayang.telemetry.audit;

import java.util.Map;

/**
 * AuditLogger is responsible for recording security-sensitive events 
 * (like tool executions) in a structured format suitable for external 
 * compliance systems (e.g., Splunk, Datadog).
 */
public interface AuditLogger {
    
    /**
     * Logs the execution of a tool by an agent.
     *
     * @param sessionId the unique identifier for the agent session
     * @param toolName the name of the tool executed
     * @param arguments the arguments passed to the tool
     * @param status the outcome of the execution (e.g., SUCCESS, FAILURE)
     * @param userContext contextual information about the user/tenant running the agent
     */
    void logToolExecution(String sessionId, String toolName, Map<String, Object> arguments, String status, String userContext);
}
