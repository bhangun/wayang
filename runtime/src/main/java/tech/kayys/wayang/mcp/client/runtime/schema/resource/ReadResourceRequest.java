package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;

public class ReadResourceRequest extends MCPRequest {
    @Valid
    public ReadResourceParams params;
    
    @Override
    public String method() { return "resources/read"; }
}
