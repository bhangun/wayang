package tech.kayys.wayang.core.node;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Node execution context - carries all runtime state
 */
public interface NodeContext {
    
    // Execution metadata
    String getRunId();
    String getNodeId();
    String getTenantId();
    String getTraceId();
    
    // Input/output handling
    <T> T getInput(String portName);
    Map<String, Object> getAllInputs();
    void setOutput(String portName, Object value);
    
    // Variable access (workflow-level)
    <T> T getVariable(String name);
    void setVariable(String name, Object value);
    
    // Metadata and execution info
    Map<String, Object> getMetadata();
    
    // Observability
    void emitEvent(String eventType, Map<String, Object> payload);
    Logger getLogger();
    
    // Create child context for iterations
    NodeContext createChild();
}
