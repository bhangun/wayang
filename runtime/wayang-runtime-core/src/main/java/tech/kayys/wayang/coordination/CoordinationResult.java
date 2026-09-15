package tech.kayys.wayang.coordination;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of a coordination execution.
 */
public record CoordinationResult(
        String executionId,
        Status status,
        Object result,
        String error,
        Instant startedAt,
        Instant completedAt,
        List<String> participatingAgents,
        Map<String, Object> metadata
) {

    public enum Status {
        COMPLETED,
        FAILED,
        PARTIAL,
        CANCELLED
    }

    public static CoordinationResult completed(String executionId, Object result) {
        return new CoordinationResult(
                executionId,
                Status.COMPLETED,
                result,
                null,
                Instant.now(),
                Instant.now(),
                List.of(),
                Map.of()
        );
    }

    public static CoordinationResult failed(String executionId, String error) {
        return new CoordinationResult(
                executionId,
                Status.FAILED,
                null,
                error,
                Instant.now(),
                Instant.now(),
                List.of(),
                Map.of()
        );
    }

    public boolean success() {
        return status == Status.COMPLETED;
    }
}
