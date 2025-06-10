package tech.kayys.wayang.mcp.client.runtime.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.mcp.client.runtime.exception.MCPException;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;
import tech.kayys.wayang.mcp.client.runtime.transport.MCPTransport;
import tech.kayys.wayang.mcp.client.runtime.transport.MCPTransportFactory;
import tech.kayys.wayang.mcp.client.runtime.config.MCPServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class MCPConnectionManager {
    
    private static final Logger log = LoggerFactory.getLogger(MCPConnectionManager.class);
    
    private final Map<String, MCPTransport> transports = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<MCPResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final AtomicLong requestIdGenerator = new AtomicLong(1);
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    
    @Inject
    MCPTransportFactory transportFactory;
    
    @Inject
    ObjectMapper objectMapper;
    
    /**
     * Initialize a connection to an MCP server
     * 
     * @param serverName the name of the server to connect to
     * @param serverConfig the server configuration
     * @return a CompletableFuture that completes when the connection is established
     * @throws MCPException if the connection fails
     */
    public CompletableFuture<Void> initializeConnection(String serverName, MCPServerConfig serverConfig) {
        if (shuttingDown.get()) {
            throw new MCPException("Connection manager is shutting down");
        }
        
        log.debug("Initializing connection to MCP server: {}", serverName);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create and initialize transport
                MCPTransport transport = transportFactory.createTransport(serverConfig.transport());
                transport.setMessageHandler(message -> handleIncomingMessage(serverName, message));
                
                // Use Reactor's Mono for connect (reactive)
                Duration timeout = serverConfig.transport().connectionTimeout();
                transport.connect().block(timeout);
                transports.put(serverName, transport);
                
                log.info("Successfully connected to MCP server: {}", serverName);
                return null;
            } catch (Exception e) {
                log.error("Failed to initialize connection to MCP server: {}", serverName, e);
                cleanup(serverName);
                throw new MCPException("Failed to initialize MCP connection: " + serverName, e);
            }
        });
    }
    
    /**
     * Send a request to an MCP server
     * 
     * @param serverName the name of the server to send the request to
     * @param request the request to send
     * @return a CompletableFuture that completes with the response
     * @throws MCPException if the request fails
     */
    public CompletableFuture<MCPResponse> sendRequest(String serverName, MCPRequest request) {
        if (shuttingDown.get()) {
            throw new MCPException("Connection manager is shutting down");
        }
        
        MCPTransport transport = transports.get(serverName);
        if (transport == null) {
            throw new MCPException("No connection to server: " + serverName);
        }
        
        if (!transport.isConnected()) {
            throw new MCPException("Not connected to server: " + serverName);
        }
        
        CompletableFuture<MCPResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.id.toString(), future);
        
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            transport.sendMessage(requestJson);
        } catch (Exception e) {
            pendingRequests.remove(request.id.toString());
            throw new MCPException("Failed to send request to server: " + serverName, e);
        }
        
        return future;
    }
    
    /**
     * Handle an incoming message from a server
     * 
     * @param serverName the name of the server that sent the message
     * @param message the message to handle
     */
    private void handleIncomingMessage(String serverName, String message) {
        try {
            MCPResponse response = objectMapper.readValue(message, MCPResponse.class);
            CompletableFuture<MCPResponse> future = pendingRequests.remove(response.id.toString());
            
            if (future != null) {
                future.complete(response);
            } else {
                log.warn("Received response for unknown request ID: {}", response.id);
            }
        } catch (Exception e) {
            log.error("Failed to handle incoming message from server: {}", serverName, e);
        }
    }
    
    /**
     * Clean up resources for a server connection
     * 
     * @param serverName the name of the server to clean up
     */
    private void cleanup(String serverName) {
        MCPTransport transport = transports.remove(serverName);
        if (transport != null) {
            try {
                // Use Reactor's Mono for disconnect (reactive)
                transport.disconnect().block(Duration.ofSeconds(5));
            } catch (Exception e) {
                log.warn("Error disconnecting transport for server: {}", serverName, e);
            }
        }
    }
    
    /**
     * Shutdown the connection manager and all connections
     * 
     * @return a CompletableFuture that completes when all connections are closed
     */
    public CompletableFuture<Void> shutdown() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Shutting down MCP connection manager");
        
        CompletableFuture<Void>[] futures = transports.entrySet().stream()
            .map(entry -> {
                String serverName = entry.getKey();
                MCPTransport transport = entry.getValue();
                return CompletableFuture.runAsync(() -> {
                    try {
                        transport.disconnect().block(Duration.ofSeconds(5));
                        log.info("Disconnected from server: {}", serverName);
                    } catch (Exception e) {
                        log.warn("Error disconnecting from server: {}", serverName, e);
                    }
                });
            })
            .toArray(CompletableFuture[]::new);
        
        return CompletableFuture.allOf(futures)
            .whenComplete((result, error) -> {
                transports.clear();
                pendingRequests.clear();
                shuttingDown.set(false);
                if (error != null) {
                    log.error("Error during shutdown", error);
                } else {
                    log.info("MCP connection manager shutdown complete");
                }
            });
    }
}
