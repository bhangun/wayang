package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.constraints.NotNull;

public class TextPromptContent extends PromptContent {
    @NotNull
    public String text;
    
    @Override
    public String type() { return "text"; }
}
