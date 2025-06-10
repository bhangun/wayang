package tech.kayys.wayang.mcp.client.runtime.schema;

import jakarta.validation.Valid;

public class InitializeRequest extends MCPRequest {
    @Valid
    public ClientCapabilities params;
    
    @Override
    public String method() { return "initialize"; }
}
