package tech.kayys.wayang.mcp.client.runtime.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.annotation.PreDestroy;

import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClient;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClientQualifier;
import tech.kayys.wayang.mcp.client.runtime.config.MCPServerConfig;
import tech.kayys.wayang.mcp.client.runtime.config.MCPRuntimeConfig;
import tech.kayys.wayang.mcp.client.runtime.exception.MCPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

/**
 * CDI producer for MCP client instances
 */
@ApplicationScoped
public class MCPClientProducer {
    
    private static final Logger log = LoggerFactory.getLogger(MCPClientProducer.class);
    
    @Inject
    MCPClientFactory clientFactory;
    
    @Inject
    MCPRuntimeConfig runtimeConfig;
    
    // Cache for client instances
    private final ConcurrentMap<String, Object> clientCache = new ConcurrentHashMap<>();
    
    /**
     * Produces MCP client instances based on injection point
     * 
     * @param injectionPoint the injection point
     * @return the client instance
     * @throws IllegalArgumentException if the injection point is invalid
     */
    @Produces
    @MCPClientQualifier
    public Object produceMCPClient(InjectionPoint injectionPoint) {
        Type type = injectionPoint.getType();
        if (!(type instanceof Class)) {
            throw new IllegalArgumentException("Injection point type must be a Class");
        }
        
        @SuppressWarnings("unchecked")
        Class<?> clientType = (Class<?>) type;
        MCPClientQualifier qualifier = injectionPoint.getAnnotated().getAnnotation(MCPClientQualifier.class);
        
        if (qualifier == null || qualifier.value().isEmpty()) {
            throw new IllegalArgumentException("Injection point must be annotated with @MCPClientQualifier and specify a value");
        }
        
        String serverName = qualifier.value();
        
        // Check if server is configured
        MCPServerConfig serverConfig = runtimeConfig.servers().get(serverName);
        if (serverConfig == null) {
            throw new IllegalArgumentException("No configuration found for MCP server: " + serverName);
        }
        
        // Get or create client instance
        return clientCache.computeIfAbsent(serverName, name -> {
            try {
                log.debug("Creating new MCP client for server: {}", serverName);
                return clientFactory.createClient(clientType);
            } catch (Exception e) {
                log.error("Failed to create MCP client for server: {}", serverName, e);
                throw new MCPException("Failed to create MCP client", e);
            }
        });
    }
    
    /**
     * Programmatically create client with custom configuration
     * 
     * @param clientType the client interface type
     * @param serverName the server name
     * @return the client instance
     * @throws IllegalArgumentException if the server name is invalid
     * @throws MCPException if client creation fails
     */
    public <T> T createCustomClient(Class<T> clientType, String serverName) {
        if (serverName == null || serverName.trim().isEmpty()) {
            throw new IllegalArgumentException("Server name must be specified");
        }
        
        // Check if server is configured
        MCPServerConfig serverConfig = runtimeConfig.servers().get(serverName);
        if (serverConfig == null) {
            throw new IllegalArgumentException("No configuration found for MCP server: " + serverName);
        }
        
        @SuppressWarnings("unchecked")
        T client = (T) clientCache.computeIfAbsent(serverName, name -> {
            try {
                log.debug("Creating new custom MCP client for server: {}", serverName);
                return clientFactory.createClient(clientType);
            } catch (Exception e) {
                log.error("Failed to create custom MCP client for server: {}", serverName, e);
                throw new MCPException("Failed to create custom MCP client", e);
            }
        });
        
        return client;
    }
    
    /**
     * Get existing client by server name
     * 
     * @param serverName the server name
     * @return the client instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getClient(String serverName) {
        return (T) clientCache.get(serverName);
    }
    
    /**
     * Remove client from cache
     * 
     * @param serverName the server name
     */
    public void removeClient(String serverName) {
        Object client = clientCache.remove(serverName);
        if (client != null) {
            log.debug("Removed MCP client from cache: {}", serverName);
        }
    }
    
    /**
     * Clear all cached clients
     */
    public void clearCache() {
        clientCache.clear();
        log.debug("Cleared MCP client cache");
    }
    
    /**
     * Clean up resources on shutdown
     */
    @PreDestroy
    public void cleanup() {
        clearCache();
    }
}
