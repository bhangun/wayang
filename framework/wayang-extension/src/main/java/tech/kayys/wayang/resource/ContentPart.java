package tech.kayys.wayang.resource;

import java.util.Map;

/**
 * Represents a single piece of content with an explicit modality.
 * This unifies text, images, and other formats across messages, artifacts, and tools.
 */
public sealed interface ContentPart {

    Modality modality();
    Map<String, Object> metadata();

    record Text(String text, Map<String, Object> metadata) implements ContentPart {
        @Override public Modality modality() { return Modality.TEXT; }
    }

    record Image(Artifact artifact, Map<String, Object> metadata) implements ContentPart {
        @Override public Modality modality() { return Modality.IMAGE; }
    }

    record ToolUse(String id, String name, Map<String, Object> input, Map<String, Object> metadata) implements ContentPart {
        @Override public Modality modality() { return Modality.STRUCTURED_DATA; }
    }

    record ToolResult(String toolUseId, String content, boolean isError, Map<String, Object> metadata) implements ContentPart {
        @Override public Modality modality() { return Modality.STRUCTURED_DATA; }
    }

    // Static factories for convenience
    static ContentPart text(String text) {
        return new Text(text, Map.of());
    }

    static ContentPart image(Artifact artifact) {
        return new Image(artifact, Map.of());
    }

    static ContentPart toolUse(String id, String name, Map<String, Object> input) {
        return new ToolUse(id, name, input, Map.of());
    }

    static ContentPart toolResult(String toolUseId, String content, boolean isError) {
        return new ToolResult(toolUseId, content, isError, Map.of());
    }
}
