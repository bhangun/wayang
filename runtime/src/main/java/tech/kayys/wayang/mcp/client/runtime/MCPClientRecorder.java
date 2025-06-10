package tech.kayys.wayang.mcp.client.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.configuration.ConfigurationException;

import tech.kayys.wayang.mcp.client.runtime.client.MCPConnectionManager;
import tech.kayys.wayang.mcp.client.runtime.config.MCPRuntimeConfig;
import tech.kayys.wayang.mcp.client.runtime.config.MCPServerConfig;
import tech.kayys.wayang.mcp.client.runtime.exception.MCPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;

/**
 * Recorder for MCP client initialization
 */
@Recorder
public class MCPClientRecorder {
    
    private static final Logger log = LoggerFactory.getLogger(MCPClientRecorder.class);
    
    /**
     * Initialize MCP connections for all configured servers
     */
    public void initializeMCPConnections(MCPRuntimeConfig config, BeanContainer beanContainer) {
        log.info("Initializing MCP client connections...");
        
        try {
            MCPConnectionManager connectionManager = beanContainer.beanInstance(MCPConnectionManager.class);
            
            // Initialize connections for each configured server
            for (Map.Entry<String, MCPServerConfig> entry : config.servers().entrySet()) {
                String serverName = entry.getKey();
                MCPServerConfig serverConfig = entry.getValue();
                
                log.info("Initializing MCP connection to server: {}", serverName);
                
                CompletableFuture<Void> initFuture = connectionManager.initializeConnection(serverName, serverConfig);
                
                if (config.waitForInitialization()) {
                    try {
                        Duration timeout = serverConfig.connectionTimeout()
                            .orElse(config.initializationTimeout());
                            
                        initFuture.get(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
                        log.info("Successfully initialized MCP connection to server: {}", serverName);
                    } catch (Exception e) {
                        if (serverConfig.required()) {
                            throw new ConfigurationException(
                                String.format("Failed to initialize required MCP server: %s", serverName), e);
                        } else {
                            log.warn("Failed to initialize optional MCP server: {} - {}", 
                                serverName, e.getMessage());
                        }
                    }
                } else {
                    // Asynchronous initialization
                    initFuture.whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            if (serverConfig.required()) {
                                log.error("Failed to initialize required MCP server: {}", serverName, throwable);
                            } else {
                                log.warn("Failed to initialize optional MCP server: {} - {}", 
                                    serverName, throwable.getMessage());
                            }
                        } else {
                            log.info("Successfully initialized MCP connection to server: {}", serverName);
                        }
                    });
                }
            }
            
            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down MCP connections...");
                try {
                    connectionManager.shutdown()
                        .get(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
                    log.info("MCP connections shut down successfully");
                } catch (Exception e) {
                    log.warn("Error during MCP connection shutdown", e);
                }
            }));
            
        } catch (Exception e) {
            throw new MCPException("Failed to initialize MCP client connections", e);
        }
    }
    
    /**
     * Validate the MCP runtime configuration
     */
    public void validateConfiguration(MCPRuntimeConfig config) {
        if (config.servers().isEmpty()) {
            log.warn("No MCP servers configured");
            return;
        }

        for (Map.Entry<String, MCPServerConfig> entry : config.servers().entrySet()) {
            String serverName = entry.getKey();
            MCPServerConfig serverConfig = entry.getValue();
            
            validateServerConfig(serverName, serverConfig);
        }
    }
    
    /**
     * Validate server configuration
     */
    private void validateServerConfig(String serverName, MCPServerConfig config) {
        if (config.transport() == null) {
            throw new ConfigurationException(
                String.format("Transport configuration is required for MCP server '%s'", serverName));
        }

        switch (config.transport().type()) {
            case STDIO:
                if (config.transport().command() == null || config.transport().command().isEmpty()) {
                    throw new ConfigurationException(
                        String.format("Command is required for stdio transport in MCP server '%s'", serverName));
                }
                break;
                
            case HTTP:
                if (config.transport().url() == null) {
                    throw new ConfigurationException(
                        String.format("URL is required for HTTP transport in MCP server '%s'", serverName));
                }
                break;
                
            case WEBSOCKET:
                if (config.transport().url() == null) {
                    throw new ConfigurationException(
                        String.format("URL is required for WebSocket transport in MCP server '%s'", serverName));
                }
                break;
                
            default:
                throw new ConfigurationException(
                    String.format("Unsupported transport type '%s' for MCP server '%s'", 
                        config.transport().type(), serverName));
        }
    }
}

