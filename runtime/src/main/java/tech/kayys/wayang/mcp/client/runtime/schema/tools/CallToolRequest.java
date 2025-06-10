package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;

public class CallToolRequest extends MCPRequest {
    @Valid
    public CallToolParams params;
    
    @Override
    public String method() { return "tools/call"; }
}
