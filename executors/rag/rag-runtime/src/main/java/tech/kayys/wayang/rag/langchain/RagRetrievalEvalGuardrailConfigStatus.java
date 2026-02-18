package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record RagRetrievalEvalGuardrailConfigStatus(
        RagRetrievalEvalGuardrailConfig config,
        Instant updatedAt) {
}
