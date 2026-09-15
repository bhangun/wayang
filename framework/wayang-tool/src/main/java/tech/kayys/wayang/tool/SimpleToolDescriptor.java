package tech.kayys.wayang.tool;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import tech.kayys.wayang.descriptor.CapabilityDescriptor;
import tech.kayys.wayang.descriptor.ParameterDescriptor;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Standard immutable implementation of {@link ToolDescriptor}.
 */
public record SimpleToolDescriptor(
        String toolId,
        String name,
        String description,
        String version,
        Map<String, Object> inputSchema,
        Set<String> tags,
        Set<String> categories
) implements ToolDescriptor {

    public SimpleToolDescriptor {
        Objects.requireNonNull(name, "tool name cannot be null");
        toolId = (toolId == null || toolId.isBlank()) ? name : toolId;
        description = (description == null) ? "" : description;
        version = (version == null || version.isBlank()) ? "1.0.0" : version;
        inputSchema = (inputSchema == null) ? Collections.emptyMap() : Collections.unmodifiableMap(inputSchema);
        tags = (tags == null) ? Collections.emptySet() : Collections.unmodifiableSet(tags);
        categories = (categories == null) ? Collections.emptySet() : Collections.unmodifiableSet(categories);
    }

    public static SimpleToolDescriptor of(String name, String description, Map<String, Object> inputSchema) {
        return new SimpleToolDescriptor(name, name, description, "1.0.0", inputSchema, Set.of("tool"), Set.of("general"));
    }

    public static SimpleToolDescriptor of(String toolId, String name, String description, String version, Map<String, Object> inputSchema) {
        return new SimpleToolDescriptor(toolId, name, description, version, inputSchema, Set.of("tool"), Set.of("general"));
    }

    @Override
    public ResourceId id() {
        java.util.UUID uuid;
        try {
            uuid = java.util.UUID.fromString(toolId);
        } catch (IllegalArgumentException e) {
            uuid = java.util.UUID.nameUUIDFromBytes(toolId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return new ResourceId.ToolId(Id.fromUUID(uuid));
    }

    @Override
    public ResourceType type() {
        return new ResourceType.Tool();
    }

    @Override
    public Metadata metadata() {
        return Metadata.builder()
                .name(name)
                .description(description)
                .version(version)
                .label("type", "tool")
                .build();
    }

    @Override
    public Map<String, ParameterDescriptor> inputs() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, ParameterDescriptor> outputs() {
        return Collections.emptyMap();
    }

    @Override
    public List<CapabilityDescriptor> capabilities() {
        return Collections.emptyList();
    }
}
