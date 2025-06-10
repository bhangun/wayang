package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import jakarta.validation.constraints.NotNull;

public class ResourceToolResult extends ToolResult {
    @NotNull
    public String uri;
    
    public String mimeType;
    
    @Override
    public String type() { return "resource"; }
}
