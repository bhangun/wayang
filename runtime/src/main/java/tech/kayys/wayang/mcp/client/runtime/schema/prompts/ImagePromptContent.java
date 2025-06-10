package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.constraints.NotNull;

public class ImagePromptContent extends PromptContent {
    @NotNull
    public String data; // Base64 encoded
    
    @NotNull
    public String mimeType;
    
    @Override
    public String type() { return "image"; }
}
