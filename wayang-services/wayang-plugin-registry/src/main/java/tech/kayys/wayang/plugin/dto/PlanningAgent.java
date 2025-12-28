package tech.kayys.wayang.plugin.dto;

import java.util.Map;

import io.smallrye.mutiny.Uni;

public non-sealed interface PlanningAgent extends AgentPlugin {
    Uni<ExecutionPlan> plan(WorkflowContext context, Map<String, Object> inputs);
}
