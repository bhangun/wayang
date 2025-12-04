package tech.kayys.wayang.models.adapter.triton.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.kayys.wayang.models.adapter.triton.dto.TritonInferRequest;
import tech.kayys.wayang.models.adapter.triton.dto.TritonInferResponse;
import tech.kayys.wayang.models.adapter.triton.dto.TritonModelMetadata;
import tech.kayys.wayang.models.adapter.triton.dto.TritonServerMetadata;

/**
 * REST client for Triton Inference Server (KServe v2 protocol).
 */
@RegisterRestClient(configKey = "triton")
@Path("/v2")
public interface TritonClient {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TritonServerMetadata> getServerMetadata();
    
    @GET
    @Path("/health/ready")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TritonHealthResponse> healthReady();
    
    @GET
    @Path("/models/{model_name}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TritonModelMetadata> getModelMetadata(@PathParam("model_name") String modelName);
    
    @POST
    @Path("/models/{model_name}/infer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TritonInferResponse> infer(
        @PathParam("model_name") String modelName,
        TritonInferRequest request);
    
    record TritonHealthResponse(boolean ready) {}
}