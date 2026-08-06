package tech.kayys.wayang.input;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.extension.Metadata;
import java.util.Set;

/**
 * Input Provider - receives input from various sources.
 * 
 * <p>Input providers are responsible for receiving input from various
 * sources such as:</p>
 * <ul>
 *   <li>Chat interfaces (text, voice)</li>
 *   <li>Messaging platforms (Slack, Discord, WhatsApp)</li>
 *   <li>Event streams (Kafka, MQTT)</li>
 *   <li>File systems</li>
 *   <li>APIs (REST, gRPC)</li>
 * </ul>
 * 
 * <p>Implementations must provide their own ID, type, and metadata
 * through the {@link Extension} interface methods.</p>
 */
public interface InputProvider extends Extension {
    
    /**
     * Receive input synchronously.
     * 
     * @param context The execution context
     * @return The received request
     * @throws Exception If input reception fails
     */
    AgentRequest receive(AgentContext context) throws Exception;
    
    /**
     * Receive input asynchronously.
     * 
     * @param context The execution context
     * @return A future containing the request
     */
    default CompletableFuture<AgentRequest> receiveAsync(AgentContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return receive(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Check if streaming is supported.
     * 
     * @return true if streaming is supported
     */
    default boolean supportsStreaming() {
        return false;
    }
    
    /**
     * Start streaming input.
     * 
     * @param context The execution context
     * @param handler The stream handler
     * @throws Exception If streaming cannot be started
     */
    default void startStreaming(AgentContext context, InputStreamHandler handler) 
            throws Exception {
        throw new UnsupportedOperationException("Streaming not supported");
    }
    
    /**
     * Stop streaming input.
     * 
     * @param context The execution context
     * @throws Exception If streaming cannot be stopped
     */
    default void stopStreaming(AgentContext context) throws Exception {
        // Optional
    }
    
    /**
     * Get supported input types.
     * 
     * @return Set of supported input types
     */
    default Set<InputType> getSupportedTypes() {
        return Set.of(InputType.TEXT);
    }
    
    /**
     * Validate an input request.
     * 
     * @param request The request to validate
     * @return true if valid
     */
    default boolean validate(AgentRequest request) {
        return request != null && request.content() != null;
    }
}
