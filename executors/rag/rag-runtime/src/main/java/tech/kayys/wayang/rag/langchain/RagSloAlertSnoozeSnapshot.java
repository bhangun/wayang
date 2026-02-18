package tech.kayys.gamelan.executor.rag.langchain;

import java.time.Instant;

public record RagSloAlertSnoozeSnapshot(
        String scope,
        String fingerprint,
        Instant snoozedAt,
        Instant expiresAt,
        boolean active) {
}
