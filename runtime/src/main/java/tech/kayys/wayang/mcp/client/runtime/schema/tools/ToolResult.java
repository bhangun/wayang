package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextToolResult.class, name = "text"),
    @JsonSubTypes.Type(value = ImageToolResult.class, name = "image"),
    @JsonSubTypes.Type(value = ResourceToolResult.class, name = "resource")
})
public abstract class ToolResult {
    @NotNull
    public abstract String type();
}
