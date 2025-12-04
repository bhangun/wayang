package tech.kayys.wayang.models.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Type of AI model.
 */
public enum ModelType {
    /**
     * Large Language Model (text generation)
     */
    LLM("llm"),
    
    /**
     * Embedding model
     */
    EMBEDDING("embedding"),
    
    /**
     * Vision model
     */
    VISION("vision"),
    
    /**
     * Multimodal model
     */
    MULTIMODAL("multimodal"),
    
    /**
     * Audio/Speech model
     */
    AUDIO("audio");

    private final String value;

    ModelType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ModelType fromValue(String value) {
        for (ModelType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown model type: " + value);
    }
}