package tech.kayys.wayang.a2a.model;

import java.net.URI;
import java.util.Map;

/**
 * Represents a reference to an artifact exchanged via A2A.
 */
public record A2AArtifact(
    String id,
    String name,
    String mediaType,
    long sizeBytes,
    URI uri,
    Map<String, Object> metadata
) {}
