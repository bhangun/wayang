package tech.kayys.wayang.definition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Map;
import java.util.Set;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.descriptor.CapabilityDescriptor;
import tech.kayys.wayang.descriptor.Descriptor;
import tech.kayys.wayang.descriptor.ParameterDescriptor;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Agent Descriptor
 */
public record AgentDescriptor(
    String descriptorId,
    String name,
    String version,
    String description,
    Set<String> tags,
    Set<String> categories,
    Map<String, ParameterDescriptor> inputs,
    Map<String, ParameterDescriptor> outputs,
    List<CapabilityDescriptor> capabilities,
    List<Permission> permissions
) implements Descriptor {
    
    @Override
    public ResourceId id() {
        return new ResourceId.AgentId(Id.fromString(descriptorId));
    }
    
    @Override
    public Metadata metadata() {
        return Metadata.builder()
            .name(name)
            .description(description)
            .version(version)
            .label("type", "agent")
            .build();
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Agent();
    }
}
