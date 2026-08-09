package tech.kayys.wayang.telemetry.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of AuditLogger that emits JSON-formatted logs via SLF4J.
 * By default, it masks sensitive argument fields (like passwords, keys, tokens).
 */
@ApplicationScoped
public class Slf4jAuditLogger implements AuditLogger {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("wayang.audit");
    
    // Configurable masking behavior
    private boolean maskingEnabled = true;
    
    // Common sensitive keys that should be masked
    private final Set<String> sensitiveKeys = Set.of(
        "password", "secret", "token", "key", "authorization", "credential"
    );

    public void setMaskingEnabled(boolean maskingEnabled) {
        this.maskingEnabled = maskingEnabled;
    }

    @Override
    public void logToolExecution(String sessionId, String toolName, Map<String, Object> arguments, String status, String userContext) {
        Map<String, Object> finalArgs = maskingEnabled && arguments != null ? maskArguments(arguments) : arguments;
        
        // Construct a simple JSON string representation (in production, use a JSON library)
        String jsonEvent = String.format(
            "{\"event\":\"ToolExecution\", \"sessionId\":\"%s\", \"toolName\":\"%s\", \"status\":\"%s\", \"userContext\":\"%s\", \"arguments\":%s}",
            sessionId, toolName, status, userContext, finalArgs != null ? mapToJson(finalArgs) : "{}"
        );
        
        AUDIT_LOG.info(jsonEvent);
    }
    
    private Map<String, Object> maskArguments(Map<String, Object> arguments) {
        Map<String, Object> maskedArgs = new HashMap<>();
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            if (isSensitive(entry.getKey())) {
                maskedArgs.put(entry.getKey(), "***MASKED***");
            } else {
                maskedArgs.put(entry.getKey(), entry.getValue());
            }
        }
        return maskedArgs;
    }
    
    private boolean isSensitive(String key) {
        if (key == null) return false;
        String lowerKey = key.toLowerCase();
        return sensitiveKeys.stream().anyMatch(lowerKey::contains);
    }
    
    private String mapToJson(Map<String, Object> map) {
        return "{" + map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + (e.getValue() != null ? e.getValue().toString().replace("\"", "\\\"") : "null") + "\"")
                .collect(Collectors.joining(",")) + "}";
    }
}
