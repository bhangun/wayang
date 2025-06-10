package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import jakarta.validation.constraints.NotNull;

public class TextResourceContent extends ResourceContent {
    @NotNull
    public String text;
    
    @Override
    public String type() { return "text"; }
}
