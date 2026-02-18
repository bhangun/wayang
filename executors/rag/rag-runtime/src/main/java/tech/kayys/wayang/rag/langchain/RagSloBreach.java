package tech.kayys.gamelan.executor.rag.langchain;

public record RagSloBreach(
        String metric,
        double observed,
        double threshold,
        String severity,
        String message) {
}
