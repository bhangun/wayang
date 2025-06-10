package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;

public class ListPromptsRequest extends MCPRequest {
    public ListPromptsParams params;
    
    @Override
    public String method() { return "prompts/list"; }
}
