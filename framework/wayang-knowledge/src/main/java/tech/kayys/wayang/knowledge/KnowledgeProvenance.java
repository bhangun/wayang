package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.Map;

/**
 * Describes where a knowledge item came from.
 */
public record KnowledgeProvenance(
        String sourceUri,
        String documentId,
        String locator,
        String version,
        String revision,
        Instant observedAt,
        Map<String, Object> metadata
) {

    public KnowledgeProvenance {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        observedAt = observedAt == null ? Instant.now() : observedAt;
    }

    public static KnowledgeProvenance of(String sourceUri) {
        return new KnowledgeProvenance(
                sourceUri,
                null,
                null,
                null,
                null,
                Instant.now(),
                Map.of()
        );
    }
}
