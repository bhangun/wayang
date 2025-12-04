package tech.kayys.wayang.core.node;

import java.time.Instant;
import java.util.Map;

/**
 * Structured error payload for error-as-input pattern
 */
@lombok.Data
@lombok.Builder
public class ErrorPayload {
    private String type;
    private String message;
    private Map<String, Object> details;
    private boolean retryable;
    private String originNode;
    private String originRunId;
    private int attempt;
    private int maxAttempts;
    private Instant timestamp;
    private String suggestedAction;
    
    public static ErrorPayload from(Throwable throwable, String nodeId, NodeContext context) {
        return builder()
            .message(throwable.getMessage())
            .retryable(false)
            .originNode(nodeId)
            .originRunId(context.getRunId())
            .timestamp(Instant.now())
            .build();
    }
    
}
