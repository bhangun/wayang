package tech.kayys.wayang.execution;

import java.time.Duration;

/**
 * Retry policy for node execution.
 */
public interface RetryPolicy {

    /**
     * Checks if retry should be attempted.
     */
    boolean shouldRetry(NodeResult result, int attempt);

    /**
     * Returns the delay before the next retry.
     */
    Duration getDelay(int attempt);

    /**
     * Returns the maximum number of retries.
     */
    int getMaxRetries();

    /**
     * Returns a no-retry policy.
     */
    static RetryPolicy noRetry() {
        return NoRetryPolicy.INSTANCE;
    }

    /**
     * Returns a fixed retry policy.
     */
    static RetryPolicy fixed(int maxRetries, Duration delay) {
        return new FixedRetryPolicy(maxRetries, delay);
    }

    /**
     * Returns an exponential backoff retry policy.
     */
    static RetryPolicy exponential(int maxRetries, Duration initialDelay, double multiplier) {
        return new ExponentialRetryPolicy(maxRetries, initialDelay, multiplier);
    }

    /**
     * Returns a linear backoff retry policy.
     */
    static RetryPolicy linear(int maxRetries, Duration initialDelay, Duration increment) {
        return new LinearRetryPolicy(maxRetries, initialDelay, increment);
    }

    /**
     * No retry policy.
     */
    final class NoRetryPolicy implements RetryPolicy {
        public static final NoRetryPolicy INSTANCE = new NoRetryPolicy();

        private NoRetryPolicy() {
        }

        @Override
        public boolean shouldRetry(NodeResult result, int attempt) {
            return false;
        }

        @Override
        public Duration getDelay(int attempt) {
            return Duration.ZERO;
        }

        @Override
        public int getMaxRetries() {
            return 0;
        }
    }

    /**
     * Fixed retry policy.
     */
    final class FixedRetryPolicy implements RetryPolicy {
        private final int maxRetries;
        private final Duration delay;

        public FixedRetryPolicy(int maxRetries, Duration delay) {
            this.maxRetries = maxRetries;
            this.delay = delay;
        }

        @Override
        public boolean shouldRetry(NodeResult result, int attempt) {
            return attempt < maxRetries && !result.isSuccess();
        }

        @Override
        public Duration getDelay(int attempt) {
            return delay;
        }

        @Override
        public int getMaxRetries() {
            return maxRetries;
        }
    }

    /**
     * Exponential backoff retry policy.
     */
    final class ExponentialRetryPolicy implements RetryPolicy {
        private final int maxRetries;
        private final Duration initialDelay;
        private final double multiplier;

        public ExponentialRetryPolicy(int maxRetries, Duration initialDelay, double multiplier) {
            this.maxRetries = maxRetries;
            this.initialDelay = initialDelay;
            this.multiplier = multiplier;
        }

        @Override
        public boolean shouldRetry(NodeResult result, int attempt) {
            return attempt < maxRetries && !result.isSuccess();
        }

        @Override
        public Duration getDelay(int attempt) {
            long delayMs = (long) (initialDelay.toMillis() * Math.pow(multiplier, attempt));
            return Duration.ofMillis(delayMs);
        }

        @Override
        public int getMaxRetries() {
            return maxRetries;
        }
    }

    /**
     * Linear backoff retry policy.
     */
    final class LinearRetryPolicy implements RetryPolicy {
        private final int maxRetries;
        private final Duration initialDelay;
        private final Duration increment;

        public LinearRetryPolicy(int maxRetries, Duration initialDelay, Duration increment) {
            this.maxRetries = maxRetries;
            this.initialDelay = initialDelay;
            this.increment = increment;
        }

        @Override
        public boolean shouldRetry(NodeResult result, int attempt) {
            return attempt < maxRetries && !result.isSuccess();
        }

        @Override
        public Duration getDelay(int attempt) {
            return initialDelay.plus(increment.multipliedBy(attempt));
        }

        @Override
        public int getMaxRetries() {
            return maxRetries;
        }
    }
}