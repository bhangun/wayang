package tech.kayys.wayang.memory;

import java.time.Duration;

/**
 * Defines the rules for how long a memory lives and where it belongs.
 */
public record MemoryPolicy(
    MemoryScope scope,
    Duration retention,
    double initialImportance,
    boolean isSensitive
) {
    public static MemoryPolicy ephemeral(MemoryScope scope) {
        return new MemoryPolicy(scope, Duration.ofHours(1), 0.1, false);
    }
    
    public static MemoryPolicy permanent(MemoryScope scope) {
        return new MemoryPolicy(scope, null, 1.0, false);
    }
}
