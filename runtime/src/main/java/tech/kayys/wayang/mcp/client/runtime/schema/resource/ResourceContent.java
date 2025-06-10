package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextResourceContent.class, name = "text"),
    @JsonSubTypes.Type(value = BlobResourceContent.class, name = "blob")
})
public abstract class ResourceContent {
    @NotNull
    public String uri;
    
    public String mimeType;
    
    @NotNull
    public abstract String type();
}
