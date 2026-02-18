package tech.kayys.wayang.eip.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.eip.config.RetryConfig;

@ApplicationScoped
public class RetryService {

    private static final Logger LOG = LoggerFactory.getLogger(RetryService.class);

    public <T> Uni<T> executeWithRetry(java.util.function.Supplier<Uni<T>> operation, RetryConfig config) {
        return operation.get()
                .onFailure().retry()
                .withBackOff(config.initialDelay(), config.maxDelay())
                .withJitter(0.25)
                .atMost(config.maxAttempts() - 1)
                .onFailure().invoke(error -> LOG.error("All retry attempts exhausted after {} attempts",
                        config.maxAttempts(), error));
    }
}
