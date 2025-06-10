package tech.kayys.wayang.mcp.client.runtime.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import tech.kayys.wayang.mcp.client.runtime.schema.ClientCapabilities;
import tech.kayys.wayang.mcp.client.runtime.schema.InitializeRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.InitializeResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.PromptsCapability;
import tech.kayys.wayang.mcp.client.runtime.schema.ServerCapabilities;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.*;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.*;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.*;
import tech.kayys.wayang.mcp.client.runtime.transport.*;
import tech.kayys.wayang.mcp.client.runtime.exception.MCPException;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Optional;
import java.util.HashMap;

/**
 * Core MCP Client implementation
 */
@ApplicationScoped
public class MCPClientImpl implements MCPClientInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPClientImpl.class);
    
    @Inject
    MCPTransportFactory transportFactory;
    
    @Inject
    ObjectMapper objectMapper;
    
    @Inject
    MCPClientConfiguration config;
    
    private MCPTransport transport;
    private ServerCapabilities serverCapabilities;
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    private final Map<String, CompletableFuture<MCPResponse>> pendingRequests = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private volatile boolean connected = false;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    
    /**
     * Initialize the MCP client
     */
    @Override
    public Mono<InitializeResponse> initialize() {
        if (initialized) {
            return Mono.just(createInitializeResponse());
        }
        
        if (shuttingDown.get()) {
            return Mono.error(new MCPException("Client is shutting down"));
        }
        
        logger.info("Initializing MCP client...");
        
        return Mono.fromCallable(() -> {
            // Create transport config
            MCPTransportConfig transportConfig = new MCPTransportConfig() {
                @Override
                public MCPTransportType type() {
                    return MCPTransportType.HTTP;
                }
                
                @Override
                public String url() {
                    return config.getServerUrl();
                }
                
                @Override
                public Optional<String> command() {
                    return Optional.empty();
                }
                
                @Override
                public Duration connectionTimeout() {
                    return config.getConnectionTimeout();
                }
                
                @Override
                public Duration readTimeout() {
                    return config.getRequestTimeout();
                }
                
                @Override
                public boolean autoReconnect() {
                    return config.isAutoReconnect();
                }
                
                @Override
                public int maxReconnectAttempts() {
                    return config.getMaxReconnectAttempts();
                }
                
                @Override
                public Duration reconnectDelay() {
                    return config.getReconnectDelay();
                }

                @Override
                public Map<String, String> headers() {
                    Map<String, String> headers = new HashMap<>(config.getHeaders());
                    // Add API key if not present
                    if (!headers.containsKey("X-Goog-Api-Key")) {
                        headers.put("X-Goog-Api-Key", config.getApiKey());
                    }
                    return headers;
                }
            };
            
            // Create transport
            transport = transportFactory.createTransport(transportConfig);
            
            // Create initialize request
            InitializeRequest request = new InitializeRequest();
            request.id = generateRequestId();
            request.params = createClientCapabilities();
            
            // Send request
            CompletableFuture<MCPResponse> future = new CompletableFuture<>();
            pendingRequests.put(request.id.toString(), future);
            
            String requestJson = objectMapper.writeValueAsString(request);
            transport.sendMessage(requestJson);
            
            // Wait for response
            MCPResponse response = future.get(config.getRequestTimeout().toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            
            if (response.error != null) {
                throw new MCPException("Failed to initialize MCP client: " + response.error.message);
            }
            
            InitializeResponse initResponse = (InitializeResponse) response;
            serverCapabilities = initResponse.result;
            initialized = true;
            connected = true;
            
            logger.info("MCP client initialized successfully");
            return initResponse;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * List available resources
     */
    @Override
    public Mono<List<Resource>> listResources(String cursor) {
        ensureInitialized();
        
        ListResourcesRequest request = new ListResourcesRequest();
        request.id = generateRequestId();
        request.params = new ListResourcesParams();
        request.params.cursor = cursor;
        
        return sendRequest(request, ListResourcesResponse.class)
            .map(response -> {
                if (response.error != null) {
                    throw new MCPException("Failed to list resources: " + response.error.message);
                }
                return response.result != null ? response.result.resources : Collections.<Resource>emptyList();
            })
            .doOnSuccess(resources -> logger.debug("Listed {} resources", resources.size()))
            .onErrorResume(error -> {
                logger.error("Failed to list resources", error);
                return Mono.just(Collections.<Resource>emptyList());
            });
    }
    
    /**
     * Read a specific resource
     */
    @Override
    public Mono<List<ResourceContent>> readResource(String uri) {
        ensureInitialized();
        
        ReadResourceRequest request = new ReadResourceRequest();
        request.id = generateRequestId();
        request.params = new ReadResourceParams();
        request.params.uri = uri;
        
        return sendRequest(request, ReadResourceResponse.class)
            .map(response -> {
                if (response.error != null) {
                    throw new MCPException("Failed to read resource: " + response.error.message);
                }
                return response.result != null ? response.result.contents : Collections.<ResourceContent>emptyList();
            })
            .doOnSuccess(contents -> logger.debug("Read resource: {} with {} contents", uri, contents.size()))
            .onErrorResume(error -> {
                logger.error("Failed to read resource: {}", uri, error);
                return Mono.just(Collections.<ResourceContent>emptyList());
            });
    }
    
    /**
     * List available tools
     */
    @Override
    public Mono<List<Tool>> listTools(String cursor) {
        ensureInitialized();
        
        ListToolsRequest request = new ListToolsRequest();
        request.id = generateRequestId();
        request.params = new ListToolsParams();
        request.params.cursor = cursor;
        
        return sendRequest(request, ListToolsResponse.class)
            .map(response -> {
                if (response.error != null) {
                    throw new MCPException("Failed to list tools: " + response.error.message);
                }
                return response.result != null ? response.result.tools : Collections.<Tool>emptyList();
            })
            .doOnSuccess(tools -> logger.debug("Listed {} tools", tools.size()))
            .onErrorResume(error -> {
                logger.error("Failed to list tools", error);
                return Mono.just(Collections.<Tool>emptyList());
            });
    }
    
    /**
     * Call a specific tool
     */
    @Override
    public Mono<List<ToolResult>> callTool(String name, Map<String, Object> arguments) {
        ensureInitialized();
        
        CallToolRequest request = new CallToolRequest();
        request.id = generateRequestId();
        request.params = new CallToolParams();
        request.params.name = name;
        request.params.arguments = arguments != null ? arguments : Collections.emptyMap();
        
        return sendRequest(request, CallToolResponse.class)
            .map(response -> {
                if (response.error != null) {
                    throw new MCPException("Failed to call tool: " + response.error.message);
                }
                if (response.result != null) {
                    if (response.result.isError) {
                        throw new MCPException("Tool execution failed: " + name);
                    }
                    return response.result.content;
                }
                return Collections.<ToolResult>emptyList();
            })
            .doOnSuccess(results -> logger.debug("Called tool: {} with {} results", name, results.size()))
            .onErrorResume(error -> {
                logger.error("Failed to call tool: {}", name, error);
                return Mono.error(error);
            });
    }
    
    /**
     * List available prompts
     */
    @Override
    public Mono<List<Prompt>> listPrompts(String cursor) {
        ensureInitialized();
        
        ListPromptsRequest request = new ListPromptsRequest();
        request.id = generateRequestId();
        request.params = new ListPromptsParams();
        request.params.cursor = cursor;
        
        return sendRequest(request, ListPromptsResponse.class)
            .map(response -> {
                if (response.error != null) {
                    throw new MCPException("Failed to list prompts: " + response.error.message);
                }
                return response.result != null ? response.result.prompts : Collections.<Prompt>emptyList();
            })
            .doOnSuccess(prompts -> logger.debug("Listed {} prompts", prompts.size()))
            .onErrorResume(error -> {
                logger.error("Failed to list prompts", error);
                return Mono.just(Collections.<Prompt>emptyList());
            });
    }
    
    /**
     * Get a specific prompt
     */
    @Override
    public Mono<List<PromptMessage>> getPrompt(String name, Map<String, String> arguments) {
        ensureInitialized();
        
        GetPromptRequest request = new GetPromptRequest();
        request.id = generateRequestId();
        request.params = new GetPromptParams();
        request.params.name = name;
        request.params.arguments = arguments != null ? arguments : Collections.emptyMap();
        
        return sendRequest(request, GetPromptResponse.class)
            .map(response -> {
                if (response.error != null) {
                    throw new MCPException("Failed to get prompt: " + response.error.message);
                }
                return response.result != null ? response.result.messages : Collections.<PromptMessage>emptyList();
            })
            .doOnSuccess(messages -> logger.debug("Got prompt: {} with {} messages", name, messages.size()))
            .onErrorResume(error -> {
                logger.error("Failed to get prompt: {}", name, error);
                return Mono.just(Collections.<PromptMessage>emptyList());
            });
    }
    
    /**
     * Get server capabilities
     */
    @Override
    public ServerCapabilities getServerCapabilities() {
        return serverCapabilities;
    }
    
    /**
     * Check if client is connected
     */
    @Override
    public boolean isConnected() {
        return connected && !shuttingDown.get();
    }
    
    /**
     * Check if client is initialized
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Disconnect the client
     */
    @Override
    public Mono<Void> disconnect() {
        if (!connected || shuttingDown.get()) {
            return Mono.empty();
        }
        
        shuttingDown.set(true);
        logger.info("Disconnecting MCP client...");
        
        return Mono.fromRunnable(() -> {
            try {
                if (transport != null) {
                    transport.disconnect().block(config.getConnectionTimeout());
                }
                connected = false;
                initialized = false;
                pendingRequests.clear();
                logger.info("MCP client disconnected successfully");
            } catch (Exception e) {
                logger.error("Error disconnecting MCP client", e);
                throw new MCPException("Failed to disconnect MCP client", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
    
    /**
     * Send a request and wait for response
     */
    private <T extends MCPResponse> Mono<T> sendRequest(MCPRequest request, Class<T> responseType) {
        return Mono.fromCallable(() -> {
            CompletableFuture<MCPResponse> future = new CompletableFuture<>();
            pendingRequests.put(request.id.toString(), future);
            
            String requestJson = objectMapper.writeValueAsString(request);
            transport.sendMessage(requestJson);
            
            MCPResponse response = future.get(config.getRequestTimeout().toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            return responseType.cast(response);
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Generate a unique request ID
     */
    private String generateRequestId() {
        return String.valueOf(requestIdCounter.getAndIncrement());
    }
    
    /**
     * Create client capabilities for initialization
     */
    private ClientCapabilities createClientCapabilities() {
        ClientCapabilities capabilities = new ClientCapabilities();
        capabilities.resources = new ResourcesCapability();
        capabilities.tools = new ToolsCapability();
        capabilities.prompts = new PromptsCapability();
        return capabilities;
    }
    
    /**
     * Create initialize response
     */
    private InitializeResponse createInitializeResponse() {
        InitializeResponse response = new InitializeResponse();
        response.result = serverCapabilities;
        return response;
    }
    
    /**
     * Ensure client is initialized
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new MCPException("MCP client is not initialized");
        }
    }
    
    @PreDestroy
    public void cleanup() {
        disconnect().block();
        pendingRequests.clear();
    }
}
