package tech.kayys.wayang.mcp.client.runtime.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import java.util.function.Consumer;
import java.time.Duration;

/**
 * Abstract MCP Transport
 */
public abstract class MCPTransport {
    protected static final Logger logger = LoggerFactory.getLogger(MCPTransport.class);
    
    protected Consumer<String> messageHandler;
    protected volatile boolean connected = false;
    protected volatile boolean shuttingDown = false;
    
    /**
     * Connect to the MCP server
     * 
     * @return a Mono that completes when the connection is established
     */
    public abstract Mono<Void> connect();
    
    /**
     * Disconnect from the MCP server
     * 
     * @return a Mono that completes when the connection is closed
     */
    public abstract Mono<Void> disconnect();
    
    /**
     * Send a message to the MCP server
     * 
     * @param message the message to send
     * @throws RuntimeException if the message cannot be sent
     */
    public abstract void sendMessage(String message);
    
    /**
     * Check if the transport is connected
     * 
     * @return true if connected, false otherwise
     */
    public abstract boolean isConnected();
    
    /**
     * Set the message handler for incoming messages
     * 
     * @param handler the handler to call for incoming messages
     */
    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }
    
    /**
     * Handle an incoming message
     * 
     * @param message the message to handle
     */
    protected void handleIncomingMessage(String message) {
        if (shuttingDown) {
            logger.debug("Ignoring message during shutdown: {}", message);
            return;
        }
        
        if (messageHandler != null) {
            try {
                messageHandler.accept(message);
            } catch (Exception e) {
                logger.error("Error handling incoming message", e);
            }
        }
    }
    
    /**
     * Check if the transport is shutting down
     * 
     * @return true if shutting down, false otherwise
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }
    
    /**
     * Set the shutting down state
     * 
     * @param shuttingDown true if shutting down, false otherwise
     */
    public void setShuttingDown(boolean shuttingDown) {
        this.shuttingDown = shuttingDown;
    }
}
