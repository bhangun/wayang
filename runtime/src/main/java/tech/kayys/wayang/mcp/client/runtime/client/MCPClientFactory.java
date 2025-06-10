package tech.kayys.wayang.mcp.client.runtime.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClient;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPMethod;
import tech.kayys.wayang.mcp.client.runtime.exception.MCPException;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.CallToolRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.CallToolParams;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.CallToolResponse;
import tech.kayys.wayang.mcp.client.runtime.config.MCPRuntimeConfig;
import tech.kayys.wayang.mcp.client.runtime.config.MCPServerConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating MCP client instances
 */
@ApplicationScoped
public class MCPClientFactory {
    
    private static final Logger log = LoggerFactory.getLogger(MCPClientFactory.class);
    
    @Inject
    MCPConnectionManager connectionManager;
    
    @Inject
    ObjectMapper objectMapper;
    
    @Inject
    MCPRuntimeConfig runtimeConfig;
    
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    
    /**
     * Create a new MCP client instance
     * 
     * @param clientType the client interface type
     * @return a new client instance
     * @throws MCPException if client creation fails
     */
    public <T> T createClient(Class<T> clientType) {
        if (clientType == null) {
            throw new IllegalArgumentException("Client type cannot be null");
        }
        
        if (!clientType.isInterface()) {
            throw new IllegalArgumentException("Client type must be an interface");
        }
        
        MCPClient annotation = clientType.getAnnotation(MCPClient.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Client type must be annotated with @MCPClient");
        }
        
        String serverName = annotation.name();
        if (serverName.isEmpty()) {
            throw new IllegalArgumentException("MCPClient annotation must specify a server name");
        }
        
        // Verify server configuration exists
        MCPServerConfig serverConfig = runtimeConfig.servers().get(serverName);
        if (serverConfig == null) {
            throw new IllegalArgumentException("No configuration found for MCP server: " + serverName);
        }
        
        log.debug("Creating MCP client of type: {}", clientType.getName());
        
        try {
            // Initialize connection
            connectionManager.initializeConnection(serverName, serverConfig).get(
                serverConfig.connectionTimeout().orElse(runtimeConfig.connectionTimeout()).toMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS
            );
            
            @SuppressWarnings("unchecked")
            T client = (T) Proxy.newProxyInstance(
                clientType.getClassLoader(),
                new Class<?>[] { clientType },
                new MCPClientInvocationHandler(serverName, serverConfig)
            );
            
            log.debug("Successfully created MCP client for server: {}", serverName);
            return client;
        } catch (Exception e) {
            log.error("Failed to create MCP client", e);
            throw new MCPException("Failed to create MCP client", e);
        }
    }
    
    /**
     * Invocation handler for MCP client proxies
     */
    private class MCPClientInvocationHandler implements InvocationHandler {
        
        private final String serverName;
        private final MCPServerConfig serverConfig;
        
        public MCPClientInvocationHandler(String serverName, MCPServerConfig serverConfig) {
            this.serverName = serverName;
            this.serverConfig = serverConfig;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            
            try {
                // Get MCP method annotation
                MCPMethod mcpMethod = method.getAnnotation(MCPMethod.class);
                if (mcpMethod == null) {
                    throw new MCPException("Method must be annotated with @MCPMethod: " + method.getName());
                }

                // Create request
                CallToolRequest request = new CallToolRequest();
                request.id = generateRequestId();
                
                // Set parameters
                CallToolParams params = new CallToolParams();
                params.name = mcpMethod.method();
                
                // Convert arguments to map
                Map<String, Object> arguments = new HashMap<>();
                if (args != null && args.length > 0) {
                    java.lang.reflect.Parameter[] methodParams = method.getParameters();
                    for (int i = 0; i < methodParams.length; i++) {
                        arguments.put(methodParams[i].getName(), args[i]);
                    }
                }
                params.arguments = arguments;
                request.params = params;
                
                // Send request
                CompletableFuture<MCPResponse> future = connectionManager.sendRequest(serverName, request);
                
                // Get request timeout from configuration
                Duration timeout = serverConfig.requestTimeout()
                    .orElse(runtimeConfig.requestTimeout());
                
                // Wait for response with timeout
                MCPResponse response = future.get(
                    timeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS
                );
                
                // Handle response
                if (response.error != null) {
                    throw new MCPException("MCP method returned error: " + response.error.message);
                }
                
                // Convert response to method return type
                if (method.getReturnType() == void.class) {
                    return null;
                }
                
                CallToolResponse toolResponse = (CallToolResponse) response;
                return objectMapper.convertValue(toolResponse.result, method.getReturnType());
                
            } catch (Exception e) {
                log.error("Error invoking MCP client method: {}", method.getName(), e);
                throw new MCPException("Failed to invoke MCP client method: " + method.getName(), e);
            }
        }
    }
    
    /**
     * Generate a unique request ID
     * 
     * @return the request ID
     */
    private String generateRequestId() {
        return String.valueOf(requestIdCounter.getAndIncrement());
    }
}
