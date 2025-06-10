package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextPromptContent.class, name = "text"),
    @JsonSubTypes.Type(value = ImagePromptContent.class, name = "image"),
    @JsonSubTypes.Type(value = ResourcePromptContent.class, name = "resource")
})
public abstract class PromptContent {
    @NotNull
    public abstract String type();
}
