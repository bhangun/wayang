package tech.kayys.wayang.mcp.client.runtime.transport;

import reactor.core.publisher.Mono;

/**
 * HTTP Transport Implementation (using REST client)
 */
public class HttpTransport extends MCPTransport {
    
    private final String serverUrl;
    private final MCPHttpClient httpClient;
    
    public HttpTransport(String serverUrl, MCPHttpClient httpClient) {
        this.serverUrl = serverUrl;
        this.httpClient = httpClient;
    }
    
    @Override
    public Mono<Void> connect() {
        if (shuttingDown) {
            return Mono.error(new RuntimeException("Transport is shutting down"));
        }
        
        return Mono.fromRunnable(() -> {
            // For HTTP, connection is established per request
            connected = true;
            logger.info("HTTP transport ready for MCP server: {}", serverUrl);
        });
    }
    
    @Override
    public Mono<Void> disconnect() {
        return Mono.fromRunnable(() -> {
            connected = false;
            shuttingDown = true;
            logger.info("HTTP transport disconnected from server: {}", serverUrl);
        });
    }
    
    @Override
    public void sendMessage(String message) {
        if (shuttingDown) {
            throw new RuntimeException("Transport is shutting down");
        }
        
        if (!connected) {
            throw new RuntimeException("HTTP transport not connected");
        }
        
        try {
            String response = httpClient.sendMessage(message);
            if (response != null && !response.isEmpty()) {
                handleIncomingMessage(response);
            }
        } catch (Exception e) {
            logger.error("Failed to send HTTP message to server: {}", serverUrl, e);
            throw new RuntimeException("Failed to send HTTP message", e);
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected && !shuttingDown;
    }
}
