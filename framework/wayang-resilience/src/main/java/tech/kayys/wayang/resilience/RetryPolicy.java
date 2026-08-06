package tech.kayys.wayang.resilience;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


/**
 * Retry System
 */
public interface RetryPolicy {
    
    boolean shouldRetry(int attempt, Exception error);
    
    long waitDurationMs(int attempt);
    
    int maxAttempts();
}