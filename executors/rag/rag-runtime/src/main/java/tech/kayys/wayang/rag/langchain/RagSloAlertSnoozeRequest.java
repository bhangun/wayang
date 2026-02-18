package tech.kayys.gamelan.executor.rag.langchain;

public record RagSloAlertSnoozeRequest(
        Long durationMs,
        String scope) {
}
