package tech.kayys.wayang.mcp.client.runtime.transport;


/**
 * REST Client interface for HTTP transport
 */
@org.eclipse.microprofile.rest.client.inject.RegisterRestClient
public interface MCPHttpClient {
    
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/mcp")
    @jakarta.ws.rs.Consumes("application/json")
    @jakarta.ws.rs.Produces("application/json")
    String sendMessage(String message);
}
