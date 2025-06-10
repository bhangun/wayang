package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class ListPromptsResult {
    @NotNull
    public List<Prompt> prompts;
    
    public String nextCursor;
}
