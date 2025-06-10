package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public class GetPromptParams {
    @NotNull
    public String name;
    
    public Map<String, String> arguments;
}
