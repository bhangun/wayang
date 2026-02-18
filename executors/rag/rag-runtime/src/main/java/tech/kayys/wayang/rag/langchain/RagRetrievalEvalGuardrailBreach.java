package tech.kayys.gamelan.executor.rag.langchain;

public record RagRetrievalEvalGuardrailBreach(
        String metric,
        double observedDelta,
        double threshold,
        String message) {
}
