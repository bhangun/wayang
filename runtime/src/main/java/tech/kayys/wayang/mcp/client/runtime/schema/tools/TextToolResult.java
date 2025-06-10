package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import jakarta.validation.constraints.NotNull;

public class TextToolResult extends ToolResult {
    @NotNull
    public String text;
    
    @Override
    public String type() { return "text"; }
}
