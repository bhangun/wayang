package tech.kayys.wayang.knowledge;

import tech.kayys.wayang.knowledge.governance.KnowledgeClassification;
import tech.kayys.wayang.knowledge.governance.KnowledgeSensitivity;
import tech.kayys.wayang.knowledge.governance.KnowledgeTrustLevel;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * A normalized unit of knowledge exposed to the Wayang runtime.
 *
 * <p>Domain-neutral. Represents a document section, company rule, law article,
 * note, decision, fact, manual entry, or any domain assertion.</p>
 */
public record KnowledgeItem(
        String id,
        String sourceId,
        String type,
        String title,
        String content,
        Map<String, Object> metadata,
        KnowledgeProvenance provenance,
        KnowledgeAuthority authority,
        KnowledgeValidity validity,
        KnowledgeClassification classification,
        KnowledgeSensitivity sensitivity,
        KnowledgeTrustLevel trustLevel,
        long revision
) {

    public KnowledgeItem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceId, "sourceId");

        type = type == null || type.isBlank() ? "unknown" : type;
        title = title == null ? "" : title;
        content = content == null ? "" : content;

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        authority = authority == null ? KnowledgeAuthority.informational() : authority;
        validity = validity == null ? KnowledgeValidity.active() : validity;
        classification = classification == null ? KnowledgeClassification.INTERNAL : classification;
        sensitivity = sensitivity == null ? KnowledgeSensitivity.LOW : sensitivity;
        trustLevel = trustLevel == null ? KnowledgeTrustLevel.REVIEWED : trustLevel;
    }

    public KnowledgeItem(
            String id,
            String sourceId,
            String type,
            String title,
            String content,
            Map<String, Object> metadata,
            KnowledgeProvenance provenance,
            KnowledgeAuthority authority,
            KnowledgeValidity validity
    ) {
        this(id, sourceId, type, title, content, metadata, provenance, authority, validity,
             KnowledgeClassification.INTERNAL, KnowledgeSensitivity.LOW, KnowledgeTrustLevel.REVIEWED, 1L);
    }

    public boolean isCurrentlyValid(Instant at) {
        return validity == null || validity.isValidAt(at);
    }

    public boolean isAuthoritative() {
        return authority != null && authority.authoritative();
    }

    public KnowledgeItem withRevision(long nextRevision) {
        return new KnowledgeItem(id, sourceId, type, title, content, metadata, provenance,
                                 authority, validity, classification, sensitivity, trustLevel, nextRevision);
    }
}
