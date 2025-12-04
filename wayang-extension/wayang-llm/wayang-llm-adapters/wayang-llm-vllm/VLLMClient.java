package tech.kayys.wayang.models.adapter.vllm.client;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMRequest;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMResponse;
import tech.kayys.wayang.models.adapter.vllm.dto.VLLMStreamChunk;

/**
 * REST client for vLLM API (OpenAI-compatible).
 */
@RegisterRestClient(configKey = "vllm")
@Path("/v1")
public interface VLLMClient {
    
    @POST
    @Path("/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<VLLMResponse> completion(VLLMRequest request);
    
    @POST
    @Path("/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/event-stream")
    Multi<VLLMStreamChunk> completionStream(VLLMRequest request);
    
    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<VLLMResponse> chatCompletion(VLLMRequest request);
    
    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/event-stream")
    Multi<VLLMStreamChunk> chatCompletionStream(VLLMRequest request);
    
    @GET
    @Path("/models")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<VLLMModelsResponse> listModels();
    
    record VLLMModelsResponse(java.util.List<ModelInfo> data) {}
    record ModelInfo(String id, String object, long created, String owned_by) {}
}