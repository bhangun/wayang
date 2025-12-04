package tech.kayys.wayang.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable descriptor defining a node's contract, capabilities, and metadata.
 * This serves as the authoritative schema for node types in the platform.
 * 
 * Thread-safe and designed for caching and serialization.
 */
public record NodeDescriptor(

    @JsonProperty("id")
    String id,
    

    @JsonProperty("name")
    String name,
    

    @JsonProperty("version")
    String version,
    
    
    @JsonProperty("inputs")
    List<PortDescriptor> inputs,
    
    
    @JsonProperty("outputs")
    List<PortDescriptor> outputs,
    
    
    @JsonProperty("properties")
    List<PropertyDescriptor> properties,
    
    
    @JsonProperty("implementation")
    ImplementationDescriptor implementation,
    
    
    @JsonProperty("capabilities")
    List<String> capabilities,
    
    @JsonProperty("requiredSecrets")
    List<String> requiredSecrets,
    
    
    @JsonProperty("sandboxLevel")
    SandboxLevel sandboxLevel,
    
    @JsonProperty("resourceProfile")
    ResourceProfile resourceProfile,
    
    @JsonProperty("metadata")
    Map<String, Object> metadata,
    

    @JsonProperty("checksum")
    String checksum,
    
    @JsonProperty("signature")
    String signature,
    
    @JsonProperty("publishedBy")
    String publishedBy,
    
    
    @JsonProperty("createdAt")
    Instant createdAt,
    

    @JsonProperty("status")
    NodeStatus status
) {
    
    /**
     * Compact constructor with validation
     */
    public NodeDescriptor {
        Objects.requireNonNull(id, "Node ID cannot be null");
        Objects.requireNonNull(name, "Node name cannot be null");
        Objects.requireNonNull(version, "Version cannot be null");
        Objects.requireNonNull(inputs, "Inputs cannot be null");
        Objects.requireNonNull(outputs, "Outputs cannot be null");
        Objects.requireNonNull(properties, "Properties cannot be null");
        Objects.requireNonNull(implementation, "Implementation cannot be null");
        Objects.requireNonNull(capabilities, "Capabilities cannot be null");
        Objects.requireNonNull(sandboxLevel, "Sandbox level cannot be null");
        Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        
        // Create immutable copies
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
        properties = List.copyOf(properties);
        capabilities = List.copyOf(capabilities);
        requiredSecrets = requiredSecrets != null ? List.copyOf(requiredSecrets) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
    
    /**
     * Check if this node requires a specific capability
     */
    public boolean hasCapability(String capability) {
        return capabilities.contains(capability);
    }
    
    /**
     * Check if this node is approved for production use
     */
    public boolean isApproved() {
        return status == NodeStatus.APPROVED;
    }
    
    /**
     * Get the unique identifier combining ID and version
     */
    public String getQualifiedId() {
        return id + ":" + version;
    }
}
