package tech.kayys.wayang.knowledge.lineage;

import tech.kayys.wayang.knowledge.KnowledgeEvidence;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Complete immutable package of evidence, references, and lineage supporting a decision.
 */
public record EvidenceBundle(
        String bundleId,
        String query,
        List<KnowledgeEvidence> evidenceItems,
        List<KnowledgeLineageEdge> lineageEdges,
        Instant generatedAt,
        Map<String, Object> metadata
) {

    public EvidenceBundle {
        bundleId = bundleId == null ? "bundle-" + java.util.UUID.randomUUID() : bundleId;
        evidenceItems = evidenceItems == null ? List.of() : List.copyOf(evidenceItems);
        lineageEdges = lineageEdges == null ? List.of() : List.copyOf(lineageEdges);
        generatedAt = generatedAt == null ? Instant.now() : generatedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
