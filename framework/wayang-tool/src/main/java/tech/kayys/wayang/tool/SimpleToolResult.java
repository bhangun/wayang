package tech.kayys.wayang.tool;

import java.util.Collections;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Standard immutable implementation of {@link ToolResult}.
 */
public record SimpleToolResult(
        Map<String, Object> outputs,
        boolean success,
        String errorMessage,
        String resultId
) implements ToolResult {

    public SimpleToolResult {
        outputs = (outputs == null) ? Collections.emptyMap() : Collections.unmodifiableMap(outputs);
        resultId = (resultId == null || resultId.isBlank()) ? "tool-res-" + System.nanoTime() : resultId;
    }

    public static SimpleToolResult success(Map<String, Object> outputs) {
        return new SimpleToolResult(outputs, true, null, null);
    }

    public static SimpleToolResult failure(String errorMessage) {
        return new SimpleToolResult(Collections.emptyMap(), false, errorMessage, null);
    }

    @Override
    public Map<String, Object> getOutputs() {
        return outputs;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public ResourceId id() {
        java.util.UUID uuid;
        try {
            uuid = java.util.UUID.fromString(resultId);
        } catch (IllegalArgumentException e) {
            uuid = java.util.UUID.nameUUIDFromBytes(resultId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
                .name(resultId)
                .description("Tool execution result (success=" + success + ")")
                .label("type", "tool-result")
                .build();
    }
}
