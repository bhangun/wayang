package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record RagRetrievalEvalRun(
        String runId,
        String datasetName,
        String tenantId,
        int topK,
        double minSimilarity,
        String matchField,
        int queryCount,
        int hitCount,
        double recallAtK,
        double mrr,
        double latencyP95Ms,
        double latencyAvgMs,
        Instant evaluatedAt) {
}
