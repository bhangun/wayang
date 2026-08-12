package tech.kayys.wayang.knowledge;

import java.net.URI;

/**
 * Tracks the source of a piece of knowledge.
 */
public record Provenance(
    String sourceId,
    URI sourceUri,
    String description
) {}
