package tech.kayys.wayang.mcp.client.runtime.config;

import java.util.Map;
import java.util.Optional;

// Transport configuration
interface MCPTransportConfig {

    /**
     * Transport type: stdio, sse, websocket
     */
    String type();

    /**
     * Command for stdio transport
     */
    Optional<String> command();

    /**
     * Arguments for stdio transport
     */
    Optional<String[]> args();

    /**
     * Working directory for stdio transport
     */
    Optional<String> workingDirectory();

    /**
     * Environment variables for stdio transport
     */
    Map<String, String> env();

    /**
     * URL for SSE/WebSocket transport
     */
    Optional<String> url();

    /**
     * Headers for HTTP-based transports
     */
    Map<String, String> headers();
}
