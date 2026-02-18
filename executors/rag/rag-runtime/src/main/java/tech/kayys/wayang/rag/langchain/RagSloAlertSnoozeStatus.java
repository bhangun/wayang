package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record RagSloAlertSnoozeStatus(
        boolean active,
        String scope,
        String fingerprint,
        Instant snoozedAt,
        Instant expiresAt,
        String reason,
        Instant evaluatedAt) {
}
