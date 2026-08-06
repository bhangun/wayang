package tech.kayys.wayang.resilience;

import java.util.concurrent.Callable;

/**
 * Retry Utility
 */
public class Retry {
    
    public static <T> T retry(Callable<T> callable, RetryPolicy policy) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (!policy.shouldRetry(attempt, e)) {
                    throw e;
                }
                attempt++;
                long waitMs = policy.waitDurationMs(attempt);
                if (waitMs > 0) {
                    Thread.sleep(waitMs);
                }
            }
        }
    }
}
