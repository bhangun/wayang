package tech.kayys.wayang.models.adapter.ollama.client;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaRequest;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaResponse;
import tech.kayys.wayang.models.adapter.ollama.dto.OllamaStreamChunk;

/**
 * REST client for Ollama API.
 */
@RegisterRestClient(configKey = "ollama")
@Path("/api")
public interface OllamaClient {
    
    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OllamaResponse> generate(OllamaRequest request);
    
    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Multi<OllamaStreamChunk> generateStream(OllamaRequest request);
    
    @POST
    @Path("/chat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OllamaResponse> chat(OllamaRequest request);
    
    @POST
    @Path("/chat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Multi<OllamaStreamChunk> chatStream(OllamaRequest request);
    
    @GET
    @Path("/tags")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OllamaModelsResponse> listModels();
    
    record OllamaModelsResponse(java.util.List<ModelInfo> models) {}
    record ModelInfo(String name, long size, String digest) {}
}