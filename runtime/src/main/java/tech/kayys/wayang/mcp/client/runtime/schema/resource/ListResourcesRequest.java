package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;

public class ListResourcesRequest extends MCPRequest {
    public ListResourcesParams params;
    
    @Override
    public String method() { return "resources/list"; }
}
