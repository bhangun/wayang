package tech.kayys.wayang.knowledge;

import java.util.Map;

/**
 * Domain-neutral description of the authority attached to knowledge.
 */
public record KnowledgeAuthority(
        String kind,
        String issuer,
        int rank,
        boolean authoritative,
        Map<String, Object> metadata
) {

    public KnowledgeAuthority {
        kind = kind == null || kind.isBlank() ? "unknown" : kind;
        issuer = issuer == null ? "" : issuer;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeAuthority informational() {
        return new KnowledgeAuthority("informational", "", 0, false, Map.of());
    }

    public static KnowledgeAuthority authoritative(String kind, String issuer, int rank) {
        return new KnowledgeAuthority(kind, issuer, rank, true, Map.of());
    }
}
