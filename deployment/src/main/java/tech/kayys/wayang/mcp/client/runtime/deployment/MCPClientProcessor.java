package tech.kayys.wayang.mcp.client.runtime.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientConfiguration;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientFactory;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientProducer;
import tech.kayys.wayang.mcp.client.runtime.config.MCPConfigProducer;
import tech.kayys.wayang.mcp.client.runtime.transport.MCPTransportFactory;

class MCPClientProcessor {
    
    private static final String FEATURE = "mcp-client";
    
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
    
    @BuildStep
    AdditionalBeanBuildItem beans() {
        return AdditionalBeanBuildItem.builder()
            .addBeanClass(MCPClientConfiguration.class)
            .addBeanClass(MCPClientFactory.class)
            .addBeanClass(MCPClientProducer.class)
            .addBeanClass(MCPConfigProducer.class)
            .addBeanClass(MCPTransportFactory.class)
            .build();
    }
    
    @BuildStep
    RunTimeConfigurationDefaultBuildItem configuration() {
        return new RunTimeConfigurationDefaultBuildItem("quarkus.mcp.servers.*.transport.type", "string");
    }
} 