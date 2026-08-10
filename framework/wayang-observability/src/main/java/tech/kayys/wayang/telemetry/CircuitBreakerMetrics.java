package tech.kayys.wayang.telemetry;

import java.time.Duration;

import tech.kayys.wayang.resilience.CircuitBreaker;
import tech.kayys.wayang.resilience.CircuitBreakerState;

/**
 * Circuit breaker metrics
 */
public record CircuitBreakerMetrics(
                CircuitBreakerState state,
                int failureCount,
                int successCount,
                int totalRequests,
                double failureRate,
                Duration timeSinceStateChange,
                boolean callsPermitted,
                long estimatedRecoveryTimeMs) {

        public CircuitBreakerState state() {
                return state;
        }

        public boolean isCallPermitted() {
                return callsPermitted;
        }

        public int getFailureCount() {
                return failureCount;
        }

        public int getSuccessCount() {
                return successCount;
        }

        public int getTotalRequests() {
                return totalRequests;
        }

        public double failureRate() {
                return failureRate;
        }

        public long estimatedRecoveryTimeMs() {
                return estimatedRecoveryTimeMs;
        }
}
