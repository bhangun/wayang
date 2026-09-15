package tech.kayys.wayang.coordination;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default centralized multi-agent coordination strategy.
 */
@ApplicationScoped
public class CentralizedCoordinationStrategy implements CoordinationStrategy {

    @Override
    public String id() {
        return CoordinationStrategies.CENTRALIZED;
    }

    @Override
    public CoordinationPattern pattern() {
        return CoordinationPattern.CENTRALIZED;
    }

    @Override
    public CoordinationPlan plan(CoordinationRequest request) {
        String planId = "plan-" + UUID.randomUUID();
        String primaryAgentId = request.agent() != null && request.agent().id() != null
                ? request.agent().id().asString()
                : "primary-agent";

        List<String> participatingAgents = new ArrayList<>();
        participatingAgents.add(primaryAgentId);

        List<CoordinationPlan.CoordinationStep> steps = new ArrayList<>();

        if (request.hasAgents()) {
            for (String subAgentId : request.agentIds()) {
                if (!participatingAgents.contains(subAgentId)) {
                    participatingAgents.add(subAgentId);
                }
                String stepId = "step-" + subAgentId;
                steps.add(CoordinationPlan.CoordinationStep.of(
                        stepId,
                        subAgentId,
                        "execute",
                        List.of(),
                        Map.of("content", request.request().content())
                ));
            }

            // Aggregation step at primary agent
            List<String> subStepIds = steps.stream().map(CoordinationPlan.CoordinationStep::id).toList();
            steps.add(CoordinationPlan.CoordinationStep.of(
                    "step-aggregate-" + primaryAgentId,
                    primaryAgentId,
                    "aggregate",
                    subStepIds,
                    Map.of("task", "synthesize")
            ));
        } else {
            // Single primary agent execution
            steps.add(CoordinationPlan.CoordinationStep.of(
                    "step-" + primaryAgentId,
                    primaryAgentId,
                    "execute",
                    List.of(),
                    Map.of("content", request.request().content())
            ));
        }

        return new CoordinationPlan(
                planId,
                id(),
                pattern(),
                participatingAgents,
                steps,
                Map.of("created", Instant.now().toString())
        );
    }

    @Override
    public Uni<CoordinationResult> execute(CoordinationPlan plan, CoordinationContext context) {
        return Uni.createFrom().item(() -> new CoordinationResult(
                context.executionId().toString(),
                CoordinationResult.Status.COMPLETED,
                Map.of("planId", plan.id(), "steps", plan.steps().size()),
                null,
                context.startedAt(),
                Instant.now(),
                plan.agentIds(),
                Map.of("strategy", id())
        ));
    }
}
