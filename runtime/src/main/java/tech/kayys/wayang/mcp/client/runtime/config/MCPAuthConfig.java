package tech.kayys.wayang.mcp.client.runtime.config;

import java.util.Map;
import java.util.Optional;

// Authentication configuration
public interface MCPAuthConfig {

    /**
     * Authentication type
     */
    String type();

    /**
     * Authentication token
     */
    Optional<String> token();

    /**
     * API key
     */
    Optional<String> apiKey();

    /**
     * Username for basic auth
     */
    Optional<String> username();

    /**
     * Password for basic auth
     */
    Optional<String> password();

    /**
     * Additional auth parameters
     */
    Map<String, String> parameters();
}
