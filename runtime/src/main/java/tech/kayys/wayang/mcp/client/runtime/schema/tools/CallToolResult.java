package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class CallToolResult {
    @NotNull
    public List<ToolResult> content;
    
    public boolean isError;
}
