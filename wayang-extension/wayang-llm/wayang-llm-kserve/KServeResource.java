package tech.kayys.wayang.models.api.rest;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.kayys.wayang.models.kserve.dto.*;
import tech.kayys.wayang.models.kserve.service.KServeProtocolService;

/**
 * KServe V2 inference protocol REST API.
 * 
 * Implements: https://kserve.github.io/website/docs/concepts/architecture/data-plane/v2-protocol
 */
@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KServe V2 Protocol", description = "KServe V2 inference protocol endpoints")
public class KServeResource {
    
    @Inject
    KServeProtocolService kserveService;
    
    // ========== Server APIs ==========
    
    @GET
    @Path("/health/live")
    @Operation(summary = "Server liveness", description = "Check if server is alive")
    public Uni<ServerHealthResponse> serverLive() {
        return kserveService.isServerAlive()
            .onItem().transform(alive -> new ServerHealthResponse(alive));
    }
    
    @GET
    @Path("/health/ready")
    @Operation(summary = "Server readiness", description = "Check if server is ready")
    public Uni<ServerHealthResponse> serverReady() {
        return kserveService.isServerReady()
            .onItem().transform(ready -> new ServerHealthResponse(ready));
    }
    
    @GET
    @Operation(summary = "Server metadata", description = "Get server metadata")
    public Uni<ServerMetadataResponse> serverMetadata() {
        return kserveService.getServerMetadata();
    }
    
    // ========== Model APIs ==========
    
    @GET
    @Path("/models/{model_name}")
    @Operation(summary = "Model metadata", description = "Get model metadata")
    public Uni<ModelMetadataResponse> modelMetadata(
            @PathParam("model_name") String modelName,
            @QueryParam("version") String modelVersion) {
        return kserveService.getModelMetadata(modelName, modelVersion);
    }
    
    @GET
    @Path("/models/{model_name}/ready")
    @Operation(summary = "Model readiness", description = "Check if model is ready")
    public Uni<ModelReadyResponse> modelReady(
            @PathParam("model_name") String modelName,
            @QueryParam("version") String modelVersion) {
        return kserveService.isModelReady(modelName, modelVersion);
    }
    
    // ========== Inference APIs ==========
    
    @POST
    @Path("/models/{model_name}/infer")
    @Operation(summary = "Model inference", description = "Execute model inference")
    public Uni<InferenceResponse> modelInfer(
            @PathParam("model_name") String modelName,
            @QueryParam("version") String modelVersion,
            @Valid InferenceRequest request) {
        
        // Set model name from path if not in request
        if (request.getModelName() == null) {
            request.setModelName(modelName);
        }
        if (modelVersion != null && request.getModelVersion() == null) {
            request.setModelVersion(modelVersion);
        }
        
        return kserveService.infer(request);
    }
    
    // Health response record
    public record ServerHealthResponse(boolean ready) {}
}