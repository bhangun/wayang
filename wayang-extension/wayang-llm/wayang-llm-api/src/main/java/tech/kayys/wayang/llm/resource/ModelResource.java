package tech.kayys.wayang.models.api.rest;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.kayys.wayang.models.api.dto.ModelRequest;
import tech.kayys.wayang.models.api.dto.ModelResponse;
import tech.kayys.wayang.models.api.dto.StreamChunk;
import tech.kayys.wayang.models.api.service.ModelService;
import tech.kayys.wayang.models.metrics.ModelMetrics;
import tech.kayys.wayang.models.safety.SafetyGate;

import java.time.Duration;
import java.time.Instant;

/**
 * REST API for model inference.
 */
@Path("/api/v1/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Models", description = "Model inference API")
public class ModelResource {
    
    @Inject
    ModelService modelService;
    
    @Inject
    SafetyGate safetyGate;
    
    @Inject
    ModelMetrics metrics;
    
    @POST
    @Path("/infer")
    @Operation(summary = "Execute model inference", description = "Perform synchronous model inference")
    public Uni<ModelResponse> infer(@Valid ModelRequest request) {
        log.info("Inference request: {}", request.getRequestId());
        Instant start = Instant.now();
        
        return safetyGate.checkInput(request)
            .onItem().transformToUni(safetyCheck -> {
                if (!safetyCheck.isSafe()) {
                    log.warn("Input safety check failed: {}", safetyCheck.getViolations());
                    metrics.recordSafetyCheck(request.getTenantId(), false, "pre");
                    return Uni.createFrom().failure(
                        new WebApplicationException("Content safety violation", 400));
                }
                
                metrics.recordSafetyCheck(request.getTenantId(), true, "pre");
                return modelService.infer(request);
            })
            .onItem().transformToUni(response -> 
                safetyGate.checkOutput(response)
                    .onItem().transform(safetyCheck -> {
                        metrics.recordSafetyCheck(request.getTenantId(), 
                            safetyCheck.isSafe(), "post");
                        
                        if (!safetyCheck.isSafe() && safetyCheck.getSanitizedContent() != null) {
                            response.setContent(safetyCheck.getSanitizedContent());
                        }
                        
                        return response;
                    })
            )
            .invoke(response -> {
                Duration duration = Duration.between(start, Instant.now());
                metrics.recordInference(request, response.getModelId(), duration, true);
                metrics.recordTokens(request, response);
                metrics.recordCost(request, response);
            })
            .onFailure().invoke(throwable -> {
                Duration duration = Duration.between(start, Instant.now());
                metrics.recordInference(request, "unknown", duration, false);
                log.error("Inference failed", throwable);
            });
    }
    
    @POST
    @Path("/infer/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Operation(summary = "Execute streaming inference", description = "Perform streaming model inference")
    public Multi<StreamChunk> inferStream(@Valid ModelRequest request) {
        log.info("Streaming inference request: {}", request.getRequestId());
        
        return Multi.createFrom().uni(safetyGate.checkInput(request))
            .onItem().transformToMulti(safetyCheck -> {
                if (!safetyCheck.isSafe()) {
                    log.warn("Input safety check failed: {}", safetyCheck.getViolations());
                    metrics.recordSafetyCheck(request.getTenantId(), false, "pre");
                    return Multi.createFrom().failure(
                        new WebApplicationException("Content safety violation", 400));
                }
                
                metrics.recordSafetyCheck(request.getTenantId(), true, "pre");
                return modelService.inferStream(request);
            });
    }
    
    @GET
    @Path("/health")
    @Operation(summary = "Health check", description = "Check model service health")
    public Uni<HealthStatus> health() {
        return modelService.healthCheck()
            .onItem().transform(healthy -> new HealthStatus(healthy, "Model service is " + 
                (healthy ? "healthy" : "unhealthy")));
    }
    
    public record HealthStatus(boolean healthy, String message) {}
}