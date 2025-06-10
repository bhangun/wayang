package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import jakarta.validation.constraints.NotNull;

public class BlobResourceContent extends ResourceContent {
    @NotNull
    public String blob; // Base64 encoded
    
    @Override
    public String type() { return "blob"; }
}
