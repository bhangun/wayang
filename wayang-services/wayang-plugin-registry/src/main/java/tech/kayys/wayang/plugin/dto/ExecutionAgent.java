package tech.kayys.wayang.plugin.dto;

public non-sealed interface ExecutionAgent extends AgentPlugin {
    Uni<ExecutionResult> executeTask(Task task, ExecutionContext context);
}