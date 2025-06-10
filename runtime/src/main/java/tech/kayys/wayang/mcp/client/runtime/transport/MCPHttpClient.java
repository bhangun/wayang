package tech.kayys.wayang.mcp.client.runtime.transport;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP client for MCP transport
 */
@RegisterRestClient
public interface MCPHttpClient {
    
    public static final String API_KEY_HEADER = "X-Goog-Api-Key";
    
    /**
     * Send a message to the MCP server
     * 
     * @param message the message to send
     * @param apiKey the API key for authentication
     * @return the response from the server
     */
    @POST
    @Path("/models/gemini-1.5-pro:generateContent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    String sendMessage(String message, @QueryParam("key") String apiKey);
}
