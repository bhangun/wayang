package tech.kayys.wayang.models.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Model capabilities enumeration.
 * Defines what features a model supports.
 */
public enum ModelCapability {
    /**
     * Basic text completion
     */
    COMPLETION("completion"),
    
    /**
     * Chat/conversational interface
     */
    CHAT("chat"),
    
    /**
     * Function/tool calling
     */
    FUNCTION_CALLING("function_calling"),
    
    /**
     * Vision/image understanding
     */
    VISION("vision"),
    
    /**
     * Audio processing
     */
    AUDIO("audio"),
    
    /**
     * Embedding generation
     */
    EMBEDDING("embedding"),
    
    /**
     * Streaming responses
     */
    STREAMING("streaming"),
    
    /**
     * JSON mode / structured outputs
     */
    JSON_MODE("json_mode"),
    
    /**
     * Code generation specialized
     */
    CODE_GENERATION("code_generation"),
    
    /**
     * Long context window (32k+ tokens)
     */
    LONG_CONTEXT("long_context");

    private final String value;

    ModelCapability(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ModelCapability fromValue(String value) {
        for (ModelCapability capability : values()) {
            if (capability.value.equalsIgnoreCase(value)) {
                return capability;
            }
        }
        throw new IllegalArgumentException("Unknown capability: " + value);
    }
}