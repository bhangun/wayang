package tech.kayys.wayang.mcp.client.runtime.annotations;

import jakarta.enterprise.util.Nonbinding;
import java.lang.annotation.*;

/**
 * Marks a class as an MCP Client configuration.
 * 
 * The client's behavior is configured through application.properties using the following format:
 * 
 * quarkus.mcp.servers.{name}.transport.type=HTTP|WEBSOCKET|STDIO
 * quarkus.mcp.servers.{name}.transport.url=<server-url>
 * quarkus.mcp.servers.{name}.transport.headers.<header-name>=<header-value>
 * quarkus.mcp.servers.{name}.required=true|false
 * quarkus.mcp.servers.{name}.connection-timeout=<duration>
 * quarkus.mcp.servers.{name}.request-timeout=<duration>
 * quarkus.mcp.servers.{name}.retry.max-attempts=<number>
 * quarkus.mcp.servers.{name}.retry.initial-interval=<duration>
 * quarkus.mcp.servers.{name}.retry.max-interval=<duration>
 * quarkus.mcp.servers.{name}.retry.multiplier=<number>
 * 
 * Example:
 * quarkus.mcp.servers.gemini.transport.type=HTTP
 * quarkus.mcp.servers.gemini.transport.url=https://api.example.com
 * quarkus.mcp.servers.gemini.transport.headers.Authorization=Bearer token
 * quarkus.mcp.servers.gemini.required=true
 * quarkus.mcp.servers.gemini.connection-timeout=30s
 * quarkus.mcp.servers.gemini.request-timeout=60s
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPClient {
    /**
     * Client name identifier.
     * This name is used to match the configuration block in application.properties.
     * For example, if name="gemini", the configuration should be under "quarkus.mcp.servers.gemini.*"
     * 
     * @return the client name
     */
    @Nonbinding
    String name() default "";
}