package tech.kayys.wayang.knowledge;

import java.util.Map;

public record KnowledgeSourceDescriptor(
        String id,
        String name,
        String type,
        boolean readOnly,
        Map<String, Object> metadata
) {

    public KnowledgeSourceDescriptor(String id, String name, String type, boolean readOnly) {
        this(id, name, type, readOnly, Map.of());
    }

    public KnowledgeSourceDescriptor {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
