package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.util.Objects;

public record KnowledgeEvidenceArtifactId(
        String algorithm,
        String digest
) {
    public KnowledgeEvidenceArtifactId {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(digest, "digest");
    }

    public static KnowledgeEvidenceArtifactId of(String algorithm, String digest) {
        return new KnowledgeEvidenceArtifactId(algorithm, digest);
    }

    public static KnowledgeEvidenceArtifactId parse(String value) {
        Objects.requireNonNull(value, "value");
        int idx = value.indexOf(':');
        if (idx <= 0) {
            return new KnowledgeEvidenceArtifactId("sha256", value);
        }
        return new KnowledgeEvidenceArtifactId(value.substring(0, idx), value.substring(idx + 1));
    }

    public String value() {
        return algorithm + ":" + digest;
    }

    @Override
    public String toString() {
        return value();
    }
}
