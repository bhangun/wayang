package tech.kayys.wayang.engine;

/**
 * Error handling strategy for integration workflows.
 */
public enum ErrorStrategy {
    RETRY, // Retry with exponential backoff
    DEAD_LETTER_QUEUE, // Move failed messages to DLQ
    SKIP, // Skip failed records and continue
    FAIL_FAST, // Fail entire batch on first error
    COMPENSATE // Execute compensation logic
}
