package tech.kayys.wayang.agent.orchestrator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tech.kayys.wayang.agent.AgentExecutionPlan;
import tech.kayys.wayang.agent.AgentExecutionResult;

/**
 * Orchestration Context - Runtime state
 */
public record OrchestrationContext(
        String orchestrationId,
        String parentOrchestrationId,
        AgentExecutionPlan plan,
        Map<String, AgentExecutionResult> stepResults,
        Set<String> activeAgents,
        OrchestrationState state,
        Map<String, Object> sharedContext,
        List<OrchestrationEvent> events,
        Instant startedAt) {

    public OrchestrationContext addStepResult(String stepId, AgentExecutionResult result) {
        Map<String, AgentExecutionResult> newResults = new HashMap<>(stepResults);
        newResults.put(stepId, result);
        return new OrchestrationContext(
                orchestrationId,
                parentOrchestrationId,
                plan,
                newResults,
                activeAgents,
                state,
                sharedContext,
                events,
                startedAt);
    }

    public OrchestrationContext addEvent(OrchestrationEvent event) {
        List<OrchestrationEvent> newEvents = new ArrayList<>(events);
        newEvents.add(event);
        return new OrchestrationContext(
                orchestrationId,
                parentOrchestrationId,
                plan,
                stepResults,
                activeAgents,
                state,
                sharedContext,
                newEvents,
                startedAt);
    }
}
