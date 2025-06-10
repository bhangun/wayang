package tech.kayys.wayang.mcp.client.runtime.transport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for creating MCP transport instances
 */
@ApplicationScoped
public class MCPTransportFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPTransportFactory.class);
    private static final String API_KEY_HEADER = "X-Goog-Api-Key";
    
    @Inject
    @RestClient
    MCPHttpClient httpClient;
    
    @Inject
    WebSocketTransport webSocketTransport;
    
    @Inject
    ObjectMapper objectMapper;
    
    /**
     * Create a transport instance based on the provided configuration
     * 
     * @param config the transport configuration
     * @return a new transport instance
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public MCPTransport createTransport(MCPTransportConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Transport configuration cannot be null");
        }
        
        logger.debug("Creating transport of type: {}", config.type());
        
        try {
            switch (config.type()) {
                case WEBSOCKET:
                    validateWebSocketConfig(config);
                    return webSocketTransport;
                    
                case HTTP:
                    validateHttpConfig(config);
                    return new HttpTransport(config.url(), httpClient, objectMapper, config);
                    
                case STDIO:
                    validateStdioConfig(config);
                    String command = config.command().get();
                    String[] commandArray = command != null ? command.split("\\s+") : new String[0];
                    return new StdioTransport(commandArray);
                    
                default:
                    throw new IllegalArgumentException("Unsupported transport type: " + config.type());
            }
        } catch (Exception e) {
            logger.error("Failed to create transport of type: {}", config.type(), e);
            throw new IllegalArgumentException("Failed to create transport: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate WebSocket transport configuration
     */
    private void validateWebSocketConfig(MCPTransportConfig config) {
        if (config.url() == null || config.url().trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required for WebSocket transport");
        }
    }
    
    /**
     * Validate HTTP transport configuration
     */
    private void validateHttpConfig(MCPTransportConfig config) {
        if (config.url() == null || config.url().trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required for HTTP transport");
        }
        // API key is now passed as a query parameter, so we don't need to validate it here
    }
    
    /**
     * Validate STDIO transport configuration
     */
    private void validateStdioConfig(MCPTransportConfig config) {
        if (config.command().isEmpty() || config.command().get().trim().isEmpty()) {
            throw new IllegalArgumentException("Command is required for STDIO transport");
        }
    }
}
