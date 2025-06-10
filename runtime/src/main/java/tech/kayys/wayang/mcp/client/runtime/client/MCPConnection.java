package tech.kayys.wayang.mcp.client.runtime.client;


import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;

import tech.kayys.wayang.mcp.client.runtime.schema.MCPError;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.time.Duration;

/**
 * Manages MCP connections with automatic reconnection and message routing
 */
public abstract class MCPConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPConnection.class);
    
    protected final URI serverUri;
    protected final ObjectMapper objectMapper;
    protected final ConcurrentHashMap<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    protected final AtomicLong requestIdGenerator = new AtomicLong(1);
    
    protected volatile boolean connected = false;
    protected volatile boolean reconnecting = false;
    protected Consumer<MCPNotification> notificationHandler;
    protected Consumer<Throwable> errorHandler;
    protected Runnable connectionHandler;
    protected Runnable disconnectionHandler;
    
    // Connection configuration
    protected Duration reconnectDelay = Duration.ofSeconds(5);
    protected int maxReconnectAttempts = 10;
    protected Duration requestTimeout = Duration.ofSeconds(30);
    
    public MCPConnection(URI serverUri, ObjectMapper objectMapper) {
        this.serverUri = serverUri;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Establish connection to MCP server
     */
    public abstract Uni<Void> connect();
    
    /**
     * Close connection to MCP server
     */
    public abstract Uni<Void> disconnect();
    
    /**
     * Send request and wait for response
     */
    public abstract <T> Uni<T> sendRequest(MCPRequest request, Class<T> responseType);
    
    /**
     * Send notification (no response expected)
     */
    public abstract Uni<Void> sendNotification(MCPNotification notification);
    
    /**
     * Check if connection is active
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Check if currently reconnecting
     */
    public boolean isReconnecting() {
        return reconnecting;
    }
    
    /**
     * Set notification handler
     */
    public MCPConnection onNotification(Consumer<MCPNotification> handler) {
        this.notificationHandler = handler;
        return this;
    }
    
    /**
     * Set error handler
     */
    public MCPConnection onError(Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }
    
    /**
     * Set connection established handler
     */
    public MCPConnection onConnect(Runnable handler) {
        this.connectionHandler = handler;
        return this;
    }
    
    /**
     * Set disconnection handler
     */
    public MCPConnection onDisconnect(Runnable handler) {
        this.disconnectionHandler = handler;
        return this;
    }
    
    /**
     * Configure reconnection behavior
     */
    public MCPConnection withReconnect(Duration delay, int maxAttempts) {
        this.reconnectDelay = delay;
        this.maxReconnectAttempts = maxAttempts;
        return this;
    }
    
    /**
     * Configure request timeout
     */
    public MCPConnection withTimeout(Duration timeout) {
        this.requestTimeout = timeout;
        return this;
    }
    
    /**
     * Generate unique request ID
     */
    protected String generateRequestId() {
        return String.valueOf(requestIdGenerator.getAndIncrement());
    }
    
    /**
     * Handle incoming message
     */
    protected void handleMessage(String message) {
        try {
            // Try to parse as response first
            if (message.contains("\"id\"") && (message.contains("\"result\"") || message.contains("\"error\""))) {
                handleResponse(message);
            } else {
                // Parse as notification
                MCPNotification notification = objectMapper.readValue(message, MCPNotification.class);
                handleNotification(notification);
            }
        } catch (Exception e) {
            logger.error("Failed to handle message: {}", message, e);
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
        }
    }
    
    /**
     * Handle response message
     */
    protected void handleResponse(String message) {
        try {
            MCPResponse response = objectMapper.readValue(message, MCPResponse.class);
            String requestId = response.id.toString();
            
            PendingRequest pending = pendingRequests.remove(requestId);
            if (pending != null) {
                if (response.error != null) {
                    pending.completeExceptionally(new MCPException(response.error));
                } else {
                    pending.complete(response);
                }
            } else {
                logger.warn("Received response for unknown request ID: {}", requestId);
            }
        } catch (Exception e) {
            logger.error("Failed to handle response: {}", message, e);
        }
    }
    
    /**
     * Handle notification message
     */
    protected void handleNotification(MCPNotification notification) {
        logger.debug("Received notification: {}", notification.getMethod());
        if (notificationHandler != null) {
            try {
                notificationHandler.accept(notification);
            } catch (Exception e) {
                logger.error("Error in notification handler", e);
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
            }
        }
    }
    
    /**
     * Handle connection established
     */
    protected void handleConnect() {
        connected = true;
        reconnecting = false;
        logger.info("Connected to MCP server: {}", serverUri);
        if (connectionHandler != null) {
            connectionHandler.run();
        }
    }
    
    /**
     * Handle disconnection
     */
    protected void handleDisconnect() {
        boolean wasConnected = connected;
        connected = false;
        
        // Complete all pending requests with error
        pendingRequests.values().forEach(pending -> 
            pending.completeExceptionally(new MCPException("Connection lost")));
        pendingRequests.clear();
        
        if (wasConnected) {
            logger.info("Disconnected from MCP server: {}", serverUri);
            if (disconnectionHandler != null) {
                disconnectionHandler.run();
            }
            
            // Attempt reconnection if not manually disconnected
            if (!reconnecting && maxReconnectAttempts > 0) {
                attemptReconnect();
            }
        }
    }
    
    /**
     * Attempt to reconnect
     */
    protected void attemptReconnect() {
        if (reconnecting) return;
        
        reconnecting = true;
        logger.info("Attempting to reconnect to MCP server: {}", serverUri);
        
        Uni.createFrom().item(() -> null)
            .onItem().delayIt().by(reconnectDelay)
            .chain(ignored -> connect())
            .subscribe().with(
                success -> {
                    logger.info("Reconnected successfully");
                    reconnecting = false;
                },
                failure -> {
                    logger.error("Reconnection failed", failure);
                    reconnecting = false;
                    if (errorHandler != null) {
                        errorHandler.accept(failure);
                    }
                }
            );
    }
    
    /**
     * Pending request holder
     */
    protected static class PendingRequest {
        private final Uni<MCPResponse> uni;
        private final Consumer<MCPResponse> completer;
        private final Consumer<Throwable> errorCompleter;
        private final Cancellable cancellable;
        
        public PendingRequest() {
            final Uni[] uniHolder = new Uni[1];
            final Consumer[] completerHolder = new Consumer[1];
            final Consumer[] errorCompleterHolder = new Consumer[1];

            Uni<MCPResponse> uni = Uni.createFrom().<MCPResponse>emitter(em -> {
                completerHolder[0] = response -> em.complete((MCPResponse) response);
                errorCompleterHolder[0] = error -> em.fail((Throwable) error);
            }).ifNoItem().after(Duration.ofSeconds(30))
              .failWith(new MCPException("Request timeout"));

            this.uni = uni;
            this.completer = (Consumer<MCPResponse>) completerHolder[0];
            this.errorCompleter = (Consumer<Throwable>) errorCompleterHolder[0];
            this.cancellable = null; // Will be set by timeout
        }
        
        public void complete(MCPResponse response) {
            completer.accept(response);
        }
        
        public void completeExceptionally(Throwable error) {
            errorCompleter.accept(error);
        }
        
        public <T> Uni<T> getUni(Class<T> responseType, ObjectMapper mapper) {
            return uni.map(response -> {
                try {
                    java.lang.reflect.Field resultField = response.getClass().getField("result");
                    Object result = resultField.get(response);
                    if (result == null) {
                        return null;
                    }
                    return mapper.convertValue(result, responseType);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new MCPException("Response type does not have a public 'result' field", e);
                } catch (Exception e) {
                    throw new MCPException("Failed to deserialize response", e);
                }
            });
        }
    }
    
    /**
     * MCP specific exception
     */
    public static class MCPException extends RuntimeException {
        private final MCPError mcpError;
        
        public MCPException(String message) {
            super(message);
            this.mcpError = null;
        }
        
        public MCPException(String message, Throwable cause) {
            super(message, cause);
            this.mcpError = null;
        }
        
        public MCPException(MCPError error) {
            super(error.message);
            this.mcpError = error;
        }
        
        public MCPError getMcpError() {
            return mcpError;
        }
    }
}
