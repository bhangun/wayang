package tech.kayys.wayang.agent.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.agent.mcp.model.MCPResource;

import java.util.List;

/**
 * MCP Resource Provider interface.
 * Provides access to resources that agents can use.
 */
public interface MCPResourceProvider {

    /**
     * List all available resources.
     * 
     * @return List of available resources
     */
    Uni<List<MCPResource>> listResources();

    /**
     * Read resource content by URI.
     * 
     * @param uri The resource URI
     * @return Resource content as string
     */
    Uni<String> readResource(String uri);

    /**
     * Read resource content as bytes.
     * 
     * @param uri The resource URI
     * @return Resource content as bytes
     */
    Uni<byte[]> readResourceBytes(String uri);

    /**
     * Subscribe to resource updates.
     * 
     * @param uri     The resource URI
     * @param handler Update handler
     * @return Subscription handle
     */
    Subscription subscribe(String uri, ResourceUpdateHandler handler);

    /**
     * Check if a resource exists.
     * 
     * @param uri The resource URI
     * @return True if resource exists
     */
    Uni<Boolean> resourceExists(String uri);

    /**
     * Handler for resource updates.
     */
    @FunctionalInterface
    interface ResourceUpdateHandler {
        void onUpdate(String uri, String content);
    }

    /**
     * Subscription handle.
     */
    interface Subscription {
        void cancel();
    }
}
