package tech.kayys.wayang.coordination;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable intermediate representation of a coordination execution.
 *
 * <p>The plan describes what coordination should happen without requiring
 * the strategy implementation to directly execute every operation.</p>
 */
public record CoordinationPlan(
        String id,
        String strategyId,
        CoordinationPattern pattern,
        List<String> agentIds,
        List<CoordinationStep> steps,
        Map<String, Object> metadata
) {

    public CoordinationPlan {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(strategyId, "strategyId must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");

        agentIds = agentIds == null ? List.of() : List.copyOf(agentIds);
        steps = steps == null ? List.of() : List.copyOf(steps);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /**
     * A single coordination operation step.
     */
    public record CoordinationStep(
            String id,
            String agentId,
            String operation,
            List<String> dependsOn,
            Map<String, Object> parameters,
            Map<String, Object> metadata
    ) {

        public CoordinationStep {
            Objects.requireNonNull(id, "step id must not be null");
            Objects.requireNonNull(agentId, "agentId must not be null");

            operation = operation == null ? "execute" : operation;
            dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn);
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }

        public static CoordinationStep of(String id, String agentId, String operation) {
            return new CoordinationStep(id, agentId, operation, List.of(), Map.of(), Map.of());
        }

        public static CoordinationStep of(String id, String agentId, String operation, List<String> dependsOn, Map<String, Object> parameters) {
            return new CoordinationStep(id, agentId, operation, dependsOn, parameters, Map.of());
        }
    }
}
