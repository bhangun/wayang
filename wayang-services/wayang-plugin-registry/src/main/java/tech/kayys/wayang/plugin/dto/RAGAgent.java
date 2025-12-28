package tech.kayys.wayang.plugin.dto;

public non-sealed interface RAGAgent extends AgentPlugin {
    Uni<RAGResult> retrieveAndGenerate(String query, RAGContext context);
}