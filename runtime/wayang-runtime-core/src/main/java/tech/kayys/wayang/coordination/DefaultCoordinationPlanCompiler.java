package tech.kayys.wayang.coordination;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class DefaultCoordinationPlanCompiler implements CoordinationPlanCompiler {

    @Override
    public CoordinationWorkflowDefinition compile(CoordinationPlan plan) {
        Objects.requireNonNull(plan, "plan must not be null");

        String workflowName = "wayang-coordination-" + plan.strategyId();
        List<CoordinationWorkflowDefinition.Node> nodes = new ArrayList<>();
        List<CoordinationWorkflowDefinition.Edge> edges = new ArrayList<>();

        for (CoordinationPlan.CoordinationStep step : plan.steps()) {
            nodes.add(new CoordinationWorkflowDefinition.Node(
                    step.id(),
                    "wayang-agent",
                    step.agentId(),
                    step.operation(),
                    step.dependsOn(),
                    step.parameters(),
                    step.metadata()
            ));

            for (String dep : step.dependsOn()) {
                edges.add(new CoordinationWorkflowDefinition.Edge(dep, step.id(), null, java.util.Map.of()));
            }
        }

        return new CoordinationWorkflowDefinition(
                workflowName,
                "gamelan",
                nodes,
                edges,
                plan.metadata()
        );
    }
}
