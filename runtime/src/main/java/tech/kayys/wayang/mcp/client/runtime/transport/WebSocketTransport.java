package tech.kayys.wayang.mcp.client.runtime.transport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import tech.kayys.wayang.mcp.client.runtime.config.MCPServerConfig;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClientQualifier;

import java.net.URI;
import java.time.Duration;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * WebSocket Transport Implementation
 */
@ApplicationScoped
@ClientEndpoint
public class WebSocketTransport extends MCPTransport {
    
    @Inject
    @MCPClientQualifier("websocket")
    MCPServerConfig serverConfig;
    
    private Session session;
    private final WebSocketClient client;
    private final URI serverUri;
    private final Sinks.One<Void> connectionSink = Sinks.one();
    
    public WebSocketTransport() {
        this.client = new WebSocketClient();
        this.client.setIdleTimeout(Duration.ofSeconds(30));
        try {
            this.serverUri = new URI(serverConfig.transport().url());
        } catch (Exception e) {
            throw new RuntimeException("Invalid server URL: " + serverConfig.transport().url(), e);
        }
    }
    
    @Override
    public Mono<Void> connect() {
        if (shuttingDown) {
            return Mono.error(new RuntimeException("Transport is shutting down"));
        }
        
        return Mono.fromRunnable(() -> {
            try {
                if (!client.isStarted()) {
                    client.start();
                }
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                session = container.connectToServer(this, serverUri);
                connected = true;
                connectionSink.tryEmitValue(null);
                logger.info("Connected to MCP server via WebSocket: {}", serverUri);
            } catch (Exception e) {
                connectionSink.tryEmitError(e);
                throw new RuntimeException("Failed to connect to MCP server", e);
            }
        })
        .then(connectionSink.asMono());
    }
    
    @Override
    public Mono<Void> disconnect() {
        return Mono.fromRunnable(() -> {
            connected = false;
            shuttingDown = true;
            
            if (session != null && session.isOpen()) {
                try {
                    session.close(new CloseReason(
                        CloseReason.CloseCodes.NORMAL_CLOSURE,
                        "Client initiated disconnect"
                    ));
                    logger.info("Disconnected from MCP server via WebSocket: {}", serverUri);
                } catch (Exception e) {
                    logger.error("Error closing WebSocket session", e);
                }
            }
            
            if (client.isStarted()) {
                try {
                    client.stop();
                } catch (Exception e) {
                    logger.error("Error stopping WebSocket client", e);
                }
            }
        });
    }
    
    @Override
    public void sendMessage(String message) {
        if (shuttingDown) {
            throw new RuntimeException("Transport is shutting down");
        }
        
        if (session == null || !session.isOpen()) {
            throw new RuntimeException("WebSocket session is not connected");
        }
        
        try {
            session.getBasicRemote().sendText(message);
            logger.debug("Sent message via WebSocket: {}", message);
        } catch (Exception e) {
            logger.error("Failed to send message via WebSocket", e);
            throw new RuntimeException("Failed to send message via WebSocket", e);
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected && session != null && session.isOpen();
    }
    
    @OnOpen
    public void onOpen(Session session) {
        logger.debug("WebSocket connection opened");
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        connected = false;
        logger.info("WebSocket connection closed: {}", reason);
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket error", error);
        if (!shuttingDown) {
            connected = false;
        }
    }
    
    @OnMessage
    public void onMessage(String message) {
        handleIncomingMessage(message);
    }
}
