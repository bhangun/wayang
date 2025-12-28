package tech.kayys.wayang.plugin.dto;

public non-sealed interface AnalyticsAgent extends AgentPlugin {
    Uni<AnalyticsResult> analyze(DataSet data, AnalyticsContext context);
}