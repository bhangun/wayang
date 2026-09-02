package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Map;

public record KnowledgeSnapshotPackageResource(
        String resourceId,
        String resourceType,
        String resourceVersion,
        String fingerprint,
        String mediaType,
        byte[] content,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotPackageResource {
        content = content == null ? new byte[0] : content.clone();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
