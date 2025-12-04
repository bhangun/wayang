package tech.kayys.wayang.core.dto;

import java.util.Map;

/**
 * Port descriptor for inputs/outputs
 */
record PortDescriptor(
    String name,
    String type,
    boolean required,
    Object defaultValue,
    String description,
    Map<String, Object> schema
) {
    public PortDescriptor {
        schema = schema != null ? Map.copyOf(schema) : Map.of();
    }
}