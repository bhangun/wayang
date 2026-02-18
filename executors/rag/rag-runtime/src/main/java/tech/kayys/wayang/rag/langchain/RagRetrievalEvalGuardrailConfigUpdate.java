package tech.kayys.gamelan.executor.rag.langchain;

public record RagRetrievalEvalGuardrailConfigUpdate(
        Boolean enabled,
        Integer windowSize,
        Double recallDropMax,
        Double mrrDropMax,
        Double latencyP95IncreaseMaxMs,
        Double latencyAvgIncreaseMaxMs) {
}
