package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class Prompt {
    @NotNull
    public String name;
    
    public String description;
    
    public List<PromptArgument> arguments;
}
