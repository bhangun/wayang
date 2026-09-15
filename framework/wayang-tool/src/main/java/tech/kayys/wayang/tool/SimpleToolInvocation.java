package tech.kayys.wayang.tool;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Standard immutable implementation of {@link ToolInvocation}.
 */
public record SimpleToolInvocation(
        String name,
        Map<String, Object> arguments,
        String invocationId
) implements ToolInvocation {

    public SimpleToolInvocation {
        Objects.requireNonNull(name, "tool name cannot be null");
        arguments = (arguments == null) ? Collections.emptyMap() : Collections.unmodifiableMap(arguments);
        invocationId = (invocationId == null || invocationId.isBlank()) ? "tool-inv-" + System.nanoTime() : invocationId;
    }

    public static SimpleToolInvocation of(String name, Map<String, Object> arguments) {
        return new SimpleToolInvocation(name, arguments, null);
    }

    @Override
    public ResourceId id() {
        java.util.UUID uuid;
        try {
            uuid = java.util.UUID.fromString(invocationId);
        } catch (IllegalArgumentException e) {
            uuid = java.util.UUID.nameUUIDFromBytes(invocationId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
                .description("Invocation for tool " + name)
                .label("type", "tool-invocation")
                .build();
    }
}
