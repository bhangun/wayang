package tech.kayys.gamelan.executor.rag.langchain;

public record RagRetrievalEvalGuardrailConfig(
        boolean enabled,
        int windowSize,
        double recallDropMax,
        double mrrDropMax,
        double latencyP95IncreaseMaxMs,
        double latencyAvgIncreaseMaxMs) {
}
