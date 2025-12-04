package tech.kayys.wayang.core.dto;

import java.util.Map;

/**
 * Implementation details
 */
record ImplementationDescriptor(
        ImplementationType type,
        String coordinate,
        String digest,
        Map<String, String> additionalInfo) {
    public ImplementationDescriptor {
        additionalInfo = additionalInfo != null ? Map.copyOf(additionalInfo) : Map.of();
    }
}
