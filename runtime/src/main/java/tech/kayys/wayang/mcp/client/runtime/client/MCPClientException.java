package tech.kayys.wayang.mcp.client.runtime.client;

/**
 * MCP Client Exceptions
 */
class MCPClientException extends RuntimeException {
    public MCPClientException(String message) {
        super(message);
    }
    
    public MCPClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
