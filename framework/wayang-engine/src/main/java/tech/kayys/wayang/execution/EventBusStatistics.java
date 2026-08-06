package tech.kayys.wayang.execution;

import java.util.Map;

/**
 * Statistics for the event bus.
 */
public final class EventBusStatistics {

    private final long totalEventsPublished;
    private final long totalEventsDelivered;
    private final int totalListeners;
    private final Map<String, Long> eventTypeCounts;
    private final long averageDeliveryTimeMs;
    private final long maxDeliveryTimeMs;

    public EventBusStatistics(long totalEventsPublished, long totalEventsDelivered,
            int totalListeners, Map<String, Long> eventTypeCounts,
            long averageDeliveryTimeMs, long maxDeliveryTimeMs) {
        this.totalEventsPublished = totalEventsPublished;
        this.totalEventsDelivered = totalEventsDelivered;
        this.totalListeners = totalListeners;
        this.eventTypeCounts = eventTypeCounts != null ? Map.copyOf(eventTypeCounts) : Map.of();
        this.averageDeliveryTimeMs = averageDeliveryTimeMs;
        this.maxDeliveryTimeMs = maxDeliveryTimeMs;
    }

    public long getTotalEventsPublished() {
        return totalEventsPublished;
    }

    public long getTotalEventsDelivered() {
        return totalEventsDelivered;
    }

    public int getTotalListeners() {
        return totalListeners;
    }

    public Map<String, Long> getEventTypeCounts() {
        return eventTypeCounts;
    }

    public long getAverageDeliveryTimeMs() {
        return averageDeliveryTimeMs;
    }

    public long getMaxDeliveryTimeMs() {
        return maxDeliveryTimeMs;
    }

    public double getDeliverySuccessRate() {
        return totalEventsPublished > 0 ? (double) totalEventsDelivered / totalEventsPublished : 0.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalEventsPublished;
        private long totalEventsDelivered;
        private int totalListeners;
        private Map<String, Long> eventTypeCounts;
        private long averageDeliveryTimeMs;
        private long maxDeliveryTimeMs;

        public Builder totalEventsPublished(long totalEventsPublished) {
            this.totalEventsPublished = totalEventsPublished;
            return this;
        }

        public Builder totalEventsDelivered(long totalEventsDelivered) {
            this.totalEventsDelivered = totalEventsDelivered;
            return this;
        }

        public Builder totalListeners(int totalListeners) {
            this.totalListeners = totalListeners;
            return this;
        }

        public Builder eventTypeCounts(Map<String, Long> eventTypeCounts) {
            this.eventTypeCounts = eventTypeCounts;
            return this;
        }

        public Builder averageDeliveryTimeMs(long averageDeliveryTimeMs) {
            this.averageDeliveryTimeMs = averageDeliveryTimeMs;
            return this;
        }

        public Builder maxDeliveryTimeMs(long maxDeliveryTimeMs) {
            this.maxDeliveryTimeMs = maxDeliveryTimeMs;
            return this;
        }

        public EventBusStatistics build() {
            return new EventBusStatistics(totalEventsPublished, totalEventsDelivered,
                    totalListeners, eventTypeCounts,
                    averageDeliveryTimeMs, maxDeliveryTimeMs);
        }
    }
}
