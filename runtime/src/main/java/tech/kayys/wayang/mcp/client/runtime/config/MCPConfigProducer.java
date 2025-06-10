package tech.kayys.wayang.mcp.client.runtime.config;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.spi.InjectionPoint;
import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClientQualifier;

/**
 * Producer for MCP configuration beans
 */
@Dependent
public class MCPConfigProducer {
    
    @Inject
    MCPRuntimeConfig runtimeConfig;
    
    /**
     * Produces MCPServerConfig based on the injection point's qualifier
     */
    @Produces
    @Dependent
    @MCPClientQualifier
    public MCPServerConfig produceServerConfig(InjectionPoint injectionPoint) {
        MCPClientQualifier qualifier = injectionPoint.getAnnotated().getAnnotation(MCPClientQualifier.class);
        if (qualifier == null || qualifier.value().isEmpty()) {
            throw new IllegalArgumentException("Injection point must be annotated with @MCPClientQualifier and specify a value");
        }
        
        String serverName = qualifier.value();
        MCPServerConfig config = runtimeConfig.servers().get(serverName);
        if (config == null) {
            throw new IllegalArgumentException("No configuration found for MCP server: " + serverName);
        }
        
        return config;
    }
} 