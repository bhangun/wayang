package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.List;

/**
 * 
 * Metrics request.
 */
public class MetricsRequest {
    private Instant fromTime;
    private Instant toTime;
    private List<String> nodeIds;
    private boolean includeNodeMetrics = true;

    // Getters and setters
    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    public List<String> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

    public boolean isIncludeNodeMetrics() {
        return includeNodeMetrics;
    }

    public void setIncludeNodeMetrics(boolean includeNodeMetrics) {
        this.includeNodeMetrics = includeNodeMetrics;
    }
}
