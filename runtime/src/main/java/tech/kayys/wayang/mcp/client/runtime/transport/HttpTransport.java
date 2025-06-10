package tech.kayys.wayang.mcp.client.runtime.transport;

import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import tech.kayys.wayang.mcp.client.runtime.transport.LLMTypes.LLMRequest;
import tech.kayys.wayang.mcp.client.runtime.transport.LLMTypes.LLMResponse;

/**
 * HTTP Transport Implementation (using REST client)
 */
public class HttpTransport extends MCPTransport {
    
    private final String serverUrl;
    private final MCPHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MCPTransportConfig config;
    
    public HttpTransport(String serverUrl, MCPHttpClient httpClient, ObjectMapper objectMapper, MCPTransportConfig config) {
        this.serverUrl = serverUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.config = config;
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
            // Parse the incoming message as a map
            Map<String, Object> requestMap = objectMapper.readValue(message, Map.class);
            Map<String, Object> params = (Map<String, Object>) requestMap.get("params");
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            logger.info("Received MCP request: {}", message);
            logger.info("Parsed arguments: {}", arguments);
            
            // Create LLM request using factory method
            LLMRequest request = LLMTypes.createRequest(
                (String) arguments.get("prompt"),
                arguments.get("temperature") != null ? ((Number) arguments.get("temperature")).doubleValue() : null,
                arguments.get("topK") != null ? ((Number) arguments.get("topK")).intValue() : null,
                arguments.get("topP") != null ? ((Number) arguments.get("topP")).doubleValue() : null,
                arguments.get("maxTokens") != null ? ((Number) arguments.get("maxTokens")).intValue() : null
            );
            
            // Get API key from config
            String apiKey = config.headers().get(MCPHttpClient.API_KEY_HEADER);
            if (apiKey == null) {
                throw new RuntimeException("API key not found in transport configuration");
            }
            
            // Send request to LLM API
            String requestJson = objectMapper.writeValueAsString(request);
            logger.info("Sending request to Gemini API at URL: {}", serverUrl);
            logger.info("Request headers: {}", config.headers());
            logger.info("Request body: {}", requestJson);
            
            String llmResponseJson = httpClient.sendMessage(requestJson, apiKey);
            logger.info("Received response from Gemini API: {}", llmResponseJson);
            
            LLMResponse response = objectMapper.readValue(llmResponseJson, LLMResponse.class);
            
            // Convert response back to MCP format
            Map<String, Object> mcpResponse = Map.of(
                "id", requestMap.get("id"),
                "result", response.getCandidates().get(0).getContent().getParts().get(0).getText()
            );
            
            String responseJson = objectMapper.writeValueAsString(mcpResponse);
            handleIncomingMessage(responseJson);
            
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
