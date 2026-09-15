package tech.kayys.wayang.coordination;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.execution.ExecutionBudget;

/**
 * Input to the coordination subsystem.
 */
public record CoordinationRequest(
        AgentDefinition agent,
        AgentRequest request,
        ExecutionBudget budget,
        List<String> agentIds,
        Map<String, Object> attributes
) {

    public CoordinationRequest {
        Objects.requireNonNull(agent, "agent must not be null");
        Objects.requireNonNull(request, "request must not be null");

        agentIds = agentIds == null ? List.of() : List.copyOf(agentIds);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static CoordinationRequest of(AgentDefinition agent, AgentRequest request) {
        return new CoordinationRequest(
                agent,
                request,
                null,
                List.of(),
                Map.of()
        );
    }

    public boolean hasAgents() {
        return !agentIds.isEmpty();
    }
}
