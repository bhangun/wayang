package tech.kayys.wayang.models.adapter.openai.client;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIRequest;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIResponse;
import tech.kayys.wayang.models.adapter.openai.dto.OpenAIStreamChunk;

/**
 * REST client for OpenAI API.
 */
@RegisterRestClient(configKey = "openai")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${openai.api.key}")
@Path("/v1")
public interface OpenAIClient {
    
    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenAIResponse> chatCompletion(OpenAIRequest request);
    
    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/event-stream")
    Multi<OpenAIStreamChunk> chatCompletionStream(OpenAIRequest request);
    
    @POST
    @Path("/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenAIResponse> completion(OpenAIRequest request);
    
    @POST
    @Path("/embeddings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenAIResponse> embeddings(OpenAIRequest request);
}