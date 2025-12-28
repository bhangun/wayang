package tech.kayys.wayang.plugin.dto;

public non-sealed interface OrchestrationAgent extends AgentPlugin {
    Uni<OrchestrationResult> orchestrate(List<AgentPlugin> agents, Workflow workflow);
}