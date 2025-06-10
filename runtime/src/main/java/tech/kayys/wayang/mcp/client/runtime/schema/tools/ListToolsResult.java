package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class ListToolsResult {
    @NotNull
    public List<Tool> tools;
    
    public String nextCursor;
}
