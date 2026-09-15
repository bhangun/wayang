package tech.kayys.wayang.coordination;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Runtime context for a coordination execution.
 */
public record CoordinationContext(
        UUID executionId,
        String tenantId,
        String userId,
        CoordinationPolicy policy,
        Map<String, Object> attributes,
        Instant startedAt
) {

    public CoordinationContext {
        executionId = Objects.requireNonNull(executionId, "executionId must not be null");
        policy = policy == null ? CoordinationPolicy.defaults() : policy;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        startedAt = startedAt == null ? Instant.now() : startedAt;
    }

    public static CoordinationContext create() {
        return new CoordinationContext(
                UUID.randomUUID(),
                null,
                null,
                CoordinationPolicy.defaults(),
                Map.of(),
                Instant.now()
        );
    }

    public static CoordinationContext of(String tenantId, String userId) {
        return new CoordinationContext(
                UUID.randomUUID(),
                tenantId,
                userId,
                CoordinationPolicy.defaults(),
                Map.of(),
                Instant.now()
        );
    }
}
