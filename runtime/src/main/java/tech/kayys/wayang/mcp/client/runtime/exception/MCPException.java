package tech.kayys.wayang.mcp.client.runtime.exception;

/**
 * Base exception for all MCP-related errors
 */
public class MCPException extends RuntimeException {
    
    public MCPException(String message) {
        super(message);
    }
    
    public MCPException(String message, Throwable cause) {
        super(message, cause);
    }
}
