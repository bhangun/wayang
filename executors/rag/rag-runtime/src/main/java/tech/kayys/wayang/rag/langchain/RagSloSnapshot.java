package tech.kayys.gamelan.executor.rag.langchain;

public record RagSloSnapshot(
        double embeddingLatencyP95Ms,
        double searchLatencyP95Ms,
        double ingestLatencyP95Ms,
        double embeddingFailureRate,
        double searchFailureRate,
        long indexLagMs,
        long embeddingSuccessCount,
        long embeddingFailureCount,
        long searchSuccessCount,
        long searchFailureCount) {
}
