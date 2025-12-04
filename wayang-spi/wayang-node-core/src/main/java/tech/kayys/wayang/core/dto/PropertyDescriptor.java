package tech.kayys.wayang.core.dto;

import java.util.Map;

/**
 * Property descriptor for node configuration
 */
record PropertyDescriptor(
     String name,
    String type,
    Object defaultValue,
    boolean required,
    String description,
    Map<String, Object> validation
) {
    public PropertyDescriptor {
        validation = validation != null ? Map.copyOf(validation) : Map.of();
    }
}