package tech.kayys.wayang.rag.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Core metadata copy helpers for RAG value objects and stores.
 * Part of the wayang-rag SPI — no external dependencies.
 */
public final class RagMetadata {

    private RagMetadata() {}

    public static Map<String, Object> copy(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }

    public static Map<String, String> copyStrings(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, String> copied = new LinkedHashMap<>();
        metadata.forEach((key, value) -> {
            if (key != null && value != null) {
                copied.put(key, value);
            }
        });
        return copied.isEmpty() ? Map.of() : Collections.unmodifiableMap(copied);
    }
}
