package tech.kayys.wayang.spi.capability;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Result of executing a capability invocation.
 */
public record CapabilityInvocationResult(
        boolean success,
        Object output,
        String error,
        Map<String, Object> metadata
) {

    public CapabilityInvocationResult {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static CapabilityInvocationResult success(Object output) {
        return new CapabilityInvocationResult(true, output, null, Map.of());
    }

    public static CapabilityInvocationResult success(Object output, Map<String, Object> metadata) {
        return new CapabilityInvocationResult(true, output, null, metadata);
    }

    public static CapabilityInvocationResult failure(String error) {
        return new CapabilityInvocationResult(false, null, error, Map.of());
    }

    public static CapabilityInvocationResult failure(String error, Map<String, Object> metadata) {
        return new CapabilityInvocationResult(false, null, error, metadata);
    }

    public Optional<Object> getOutput() {
        return Optional.ofNullable(output);
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }
}
