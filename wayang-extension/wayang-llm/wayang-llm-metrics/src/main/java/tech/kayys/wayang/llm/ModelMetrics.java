package tech.kayys.wayang.models.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Metrics collection for model operations.
 */
@ApplicationScoped
@Slf4j
public class ModelMetrics {
    
    @Inject
    MeterRegistry registry;
    
    /**
     * Record inference request.
     * 
     * @param request Model request
     * @param modelId Selected model
     * @param duration Request duration
     * @param success Whether request succeeded
     */
    public void recordInference(ModelRequest request, String modelId, 
                               Duration duration, boolean success) {
        
        Counter.builder("wayang.model.requests")
            .description("Total model inference requests")
            .tag("tenant", request.getTenantId())
            .tag("model", modelId)
            .tag("type", request.getType())
            .tag("status", success ? "success" : "failed")
            .register(registry)
            .increment();
        
        Timer.builder("wayang.model.latency")
            .description("Model inference latency")
            .tag("tenant", request.getTenantId())
            .tag("model", modelId)
            .tag("type", request.getType())
            .register(registry)
            .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Record token usage.
     * 
     * @param request Model request
     * @param response Model response
     */
    public void recordTokens(ModelRequest request, ModelResponse response) {
        if (response.getTokensIn() != null) {
            Counter.builder("wayang.model.tokens.input")
                .description("Input tokens consumed")
                .tag("tenant", request.getTenantId())
                .tag("model", response.getModelId())
                .register(registry)
                .increment(response.getTokensIn());
        }
        
        if (response.getTokensOut() != null) {
            Counter.builder("wayang.model.tokens.output")
                .description("Output tokens generated")
                .tag("tenant", request.getTenantId())
                .tag("model", response.getModelId())
                .register(registry)
                .increment(response.getTokensOut());
        }
    }
    
    /**
     * Record cost.
     * 
     * @param request Model request
     * @param response Model response
     */
    public void recordCost(ModelRequest request, ModelResponse response) {
        if (response.getCostUsd() != null) {
            Counter.builder("wayang.model.cost.usd")
                .description("Model inference cost in USD")
                .tag("tenant", request.getTenantId())
                .tag("model", response.getModelId())
                .register(registry)
                .increment(response.getCostUsd().doubleValue());
        }
    }
    
    /**
     * Record cache hit/miss.
     * 
     * @param tenantId Tenant identifier
     * @param modelId Model identifier
     * @param hit Whether cache hit occurred
     */
    public void recordCacheAccess(String tenantId, String modelId, boolean hit) {
        Counter.builder("wayang.model.cache")
            .description("Cache hit/miss rate")
            .tag("tenant", tenantId)
            .tag("model", modelId)
            .tag("result", hit ? "hit" : "miss")
            .register(registry)
            .increment();
    }
    
    /**
     * Record safety check.
     * 
     * @param tenantId Tenant identifier
     * @param safe Whether content was safe
     * @param stage Pre or post inference
     */
    public void recordSafetyCheck(String tenantId, boolean safe, String stage) {
        Counter.builder("wayang.model.safety")
            .description("Safety check results")
            .tag("tenant", tenantId)
            .tag("stage", stage)
            .tag("result", safe ? "safe" : "violation")
            .register(registry)
            .increment();
    }
}