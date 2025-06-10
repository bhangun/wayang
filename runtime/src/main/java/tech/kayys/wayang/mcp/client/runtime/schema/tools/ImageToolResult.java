package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import jakarta.validation.constraints.NotNull;

public class ImageToolResult extends ToolResult {
    @NotNull
    public String data; // Base64 encoded
    
    @NotNull 
    public String mimeType;
    
    @Override
    public String type() { return "image"; }
}
