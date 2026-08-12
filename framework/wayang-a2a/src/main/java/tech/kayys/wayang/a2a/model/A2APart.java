package tech.kayys.wayang.a2a.model;

import java.util.Map;

/**
 * Represents a single multimodal part of a message in the A2A protocol.
 */
public sealed interface A2APart {
    
    record Text(String text) implements A2APart {}
    
    record Image(String mediaType, String base64Data) implements A2APart {}
    
    record ToolUse(String id, String name, Map<String, Object> input) implements A2APart {}
    
    record ToolResult(String id, String content, boolean isError) implements A2APart {}
}
