package tech.kayys.wayang.mcp.client.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.mcp.client.runtime.client.MCPConnection.MCPException;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPError;
import tech.kayys.wayang.mcp.client.runtime.schema.InitializeRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.InitializeResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.ClientCapabilities;
import tech.kayys.wayang.mcp.client.runtime.schema.ServerCapabilities;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * JSON serialization/deserialization for MCP protocol messages
 */
@ApplicationScoped
public class MCPSerializer {
    
    private final ObjectMapper objectMapper;
    
    @Inject
    public MCPSerializer() {
        this.objectMapper = createObjectMapper();
    }
    
    /**
     * Create configured ObjectMapper instance
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure basic settings
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Register modules
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(createMCPModule());
        
        return mapper;
    }
    
    /**
     * Create custom module for MCP-specific serialization
     */
    private SimpleModule createMCPModule() {
        SimpleModule module = new SimpleModule("MCP");
        
        // Custom serializers can be added here
        module.addSerializer(Instant.class, new InstantSerializer());
        module.addDeserializer(Instant.class, new InstantDeserializer());
        
        return module;
    }
    
    /**
     * Serialize object to JSON string
     */
    public String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new MCPException("Failed to serialize object: " + obj.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * Serialize object to pretty JSON string
     */
    public String serializePretty(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new MCPException("Failed to serialize object: " + obj.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * Deserialize JSON string to object
     */
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new MCPException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }
    
    /**
     * Deserialize JSON string to object with TypeReference
     */
    public <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new MCPException("Failed to deserialize JSON to " + typeRef.getType(), e);
        }
    }
    
    /**
     * Convert object to Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }
    
    /**
     * Convert Map to object
     */
    public <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }
    
    /**
     * Convert Map to object with TypeReference
     */
    public <T> T fromMap(Map<String, Object> map, TypeReference<T> typeRef) {
        return objectMapper.convertValue(map, typeRef);
    }
    
    /**
     * Create MCP request
     */
    public MCPRequest createRequest(String id, String method, Object params) {
        // Create appropriate request type based on method
        MCPRequest request;
        switch (method) {
            case "initialize":
                InitializeRequest initRequest = new InitializeRequest();
                if (params != null) {
                    initRequest.params = (ClientCapabilities) params;
                }
                request = initRequest;
                break;
            default:
                throw new MCPException("Unsupported method: " + method);
        }
        
        request.id = id;
        return request;
    }
    
    /**
     * Create MCP notification (request without id)
     */
    public MCPRequest createNotification(String method, Object params) {
        MCPRequest request = createRequest(null, method, params);
        request.id = null;
        return request;
    }
    
    /**
     * Create MCP success response
     */
    public MCPResponse createSuccessResponse(String id, Object result) {
        // Create appropriate response type based on method
        InitializeResponse response = new InitializeResponse();
        response.id = id;
        if (result != null) {
            response.result = (ServerCapabilities) result;
        }
        return response;
    }
    
    /**
     * Create MCP error response
     */
    public MCPResponse createErrorResponse(String id, int code, String message, Object data) {
        InitializeResponse response = new InitializeResponse();
        response.id = id;
        
        MCPError error = new MCPError();
        error.code = code;
        error.message = message;
        error.data = objectMapper.valueToTree(data);
        
        response.error = error;
        return response;
    }
    
    /**
     * Parse MCP message (could be request or response)
     */
    public Object parseMessage(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            
            if (node.has("method")) {
                // It's a request or notification
                return objectMapper.treeToValue(node, MCPRequest.class);
            } else if (node.has("result") || node.has("error")) {
                // It's a response
                return objectMapper.treeToValue(node, MCPResponse.class);
            } else {
                throw new MCPException("Invalid MCP message: missing method, result, or error");
            }
        } catch (IOException e) {
            throw new MCPException("Failed to parse MCP message", e);
        }
    }
    
    /**
     * Validate MCP message structure
     */
    public boolean isValidMessage(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            
            // Check for required jsonrpc field
            if (!node.has("jsonrpc") || !"2.0".equals(node.get("jsonrpc").asText())) {
                return false;
            }
            
            // Must have either method (request) or result/error (response)
            boolean hasMethod = node.has("method");
            boolean hasResult = node.has("result");
            boolean hasError = node.has("error");
            
            return hasMethod || hasResult || hasError;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extract message ID from JSON
     */
    public String extractMessageId(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode idNode = node.get("id");
            return idNode != null ? idNode.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get the underlying ObjectMapper for advanced usage
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Custom serializer for Instant to handle MCP timestamp format
     */
    private static class InstantSerializer extends com.fasterxml.jackson.databind.ser.std.StdSerializer<Instant> {
        private static final long serialVersionUID = 1L;
        
        public InstantSerializer() {
            super(Instant.class);
        }
        
        @Override
        public void serialize(Instant value, com.fasterxml.jackson.core.JsonGenerator gen, 
                            SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }
    
    /**
     * Custom deserializer for Instant from MCP timestamp format
     */
    private static class InstantDeserializer extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<Instant> {
        private static final long serialVersionUID = 1L;
        
        public InstantDeserializer() {
            super(Instant.class);
        }
        
        @Override
        public Instant deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) 
                throws IOException {
            String value = p.getValueAsString();
            try {
                return Instant.parse(value);
            } catch (Exception e) {
                throw new IOException("Failed to parse timestamp: " + value, e);
            }
        }
    }
}

