package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;

public class ListToolsRequest extends MCPRequest {
    public ListToolsParams params;
    
    @Override
    public String method() { return "tools/list"; }
}
