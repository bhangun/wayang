package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public class CallToolParams {
    @NotNull
    public String name;
    
    public Map<String, Object> arguments;
}
