package tech.kayys.wayang.schema.execution;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EscalationConfig - Escalation configuration for error handling
 */
@RegisterForReflection
public class EscalationConfig {
    private String onSeverityAtLeast = "ERROR"; // WARN, ERROR, CRITICAL
    private List<String> notify = new ArrayList<>();

    public String getOnSeverityAtLeast() {
        return onSeverityAtLeast;
    }

    public void setOnSeverityAtLeast(String onSeverityAtLeast) {
        List<String> validSeverities = Arrays.asList("WARN", "ERROR", "CRITICAL");
        if (!validSeverities.contains(onSeverityAtLeast)) {
            throw new IllegalArgumentException("Invalid severity level: " + onSeverityAtLeast);
        }
        this.onSeverityAtLeast = onSeverityAtLeast;
    }

    public List<String> getNotify() {
        return notify;
    }

    public void setNotify(List<String> notify) {
        this.notify = notify;
    }
}
