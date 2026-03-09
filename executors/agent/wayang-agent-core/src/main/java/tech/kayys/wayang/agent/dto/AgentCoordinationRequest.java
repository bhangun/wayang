package tech.kayys.wayang.agent;

import java.time.Instant;
import java.util.List;

import tech.kayys.wayang.agent.orchestrator.CoordinationConfig;
import tech.kayys.wayang.agent.orchestrator.CoordinationType;

/**
 * Agent Coordination Request
 */
public record AgentCoordinationRequest(
        String coordinationId,
        CoordinationType type,
        List<String> participatingAgents,
        String goal,
        CoordinationConfig config,
        Instant initiatedAt) {
}
