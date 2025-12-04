package tech.kayys.wayang.models.api.rest;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.kayys.wayang.models.api.domain.ModelCapability;
import tech.kayys.wayang.models.api.domain.ModelMetadata;
import tech.kayys.wayang.models.api.exception.ModelNotFoundException;
import tech.kayys.wayang.models.api.service.ModelRegistry;

import java.util.List;
import java.util.Set;

/**
 * REST API for model registry management.
 */
@Path("/api/v1/models/registry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Model Registry", description = "Model metadata management")
public class ModelRegistryResource {
    
    @Inject
    ModelRegistry registry;
    
    @POST
    @Operation(summary = "Register model", description = "Register a new model")
    public Uni<ModelMetadata> registerModel(@Valid ModelMetadata metadata) {
        log.info("Registering model: {}", metadata.getModelId());
        return registry.registerModel(metadata);
    }
    
    @GET
    @Path("/{modelId}")
    @Operation(summary = "Get model", description = "Get model metadata by ID")
    public Uni<ModelMetadata> getModel(@PathParam("modelId") String modelId) {
        return registry.getModel(modelId)
            .onItem().transform(opt -> opt.orElseThrow(() -> 
                new ModelNotFoundException(modelId)));
    }
    
    @GET
    @Operation(summary = "List models", description = "List all registered models")
    public Uni<List<ModelMetadata>> listModels(
            @QueryParam("provider") String provider,
            @QueryParam("capability") Set<ModelCapability> capabilities) {
        
        if (provider != null) {
            return registry.findByProvider(provider);
        }
        
        if (capabilities != null && !capabilities.isEmpty()) {
            return registry.findByCapabilities(capabilities);
        }
        
        return registry.listModels();
    }
    
    @PUT
    @Path("/{modelId}")
    @Operation(summary = "Update model", description = "Update model metadata")
    public Uni<ModelMetadata> updateModel(
            @PathParam("modelId") String modelId,
            @Valid ModelMetadata metadata) {
        log.info("Updating model: {}", modelId);
        return registry.updateModel(modelId, metadata);
    }
    
    @DELETE
    @Path("/{modelId}")
    @Operation(summary = "Deactivate model", description = "Deactivate a model")
    public Uni<Response> deactivateModel(@PathParam("modelId") String modelId) {
        log.info("Deactivating model: {}", modelId);
        return registry.deactivateModel(modelId)
            .onItem().transform(success -> Response.noContent().build());
    }
}