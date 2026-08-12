package tech.kayys.wayang.a2a.model;

import java.util.Map;

/**
 * Represents an error encountered in the A2A protocol.
 */
public record A2AError(
    int code,
    String message,
    Map<String, Object> data
) {}
