package tech.kayys.wayang.mcp.client.runtime.transport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating MCP transport instances
 */
@ApplicationScoped
public class MCPTransportFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPTransportFactory.class);
    
    @Inject
    @RestClient
    MCPHttpClient httpClient;
    
    @Inject
    WebSocketTransport webSocketTransport;
    
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
                    return new HttpTransport(config.url(), httpClient);
                    
                case STDIO:
                    validateStdioConfig(config);
                    String command = config.command();
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
    }
    
    /**
     * Validate STDIO transport configuration
     */
    private void validateStdioConfig(MCPTransportConfig config) {
        if (config.command() == null || config.command().trim().isEmpty()) {
            throw new IllegalArgumentException("Command is required for STDIO transport");
        }
    }
}
