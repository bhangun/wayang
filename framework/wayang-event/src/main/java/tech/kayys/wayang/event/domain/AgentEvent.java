package tech.kayys.wayang.event.domain;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Agent Events
 */
public interface AgentEvent extends EventPayload {
    
    record AgentStarted(
        AgentDefinition agent,
        String executionId,
        Map<String, Object> inputs
    ) implements AgentEvent {}
    
    record AgentPaused(
        String executionId,
        String reason
    ) implements AgentEvent {}
    
    record AgentResumed(
        String executionId
    ) implements AgentEvent {}
    
    record AgentCompleted(
        String executionId,
        AgentDefinition agent,
        List<Artifact> outputs,
        long durationMs
    ) implements AgentEvent {}
    
    record AgentFailed(
        String executionId,
        String error,
        Throwable cause
    ) implements AgentEvent {}
    
    record AgentCancelled(
        String executionId,
        String reason
    ) implements AgentEvent {}
}
