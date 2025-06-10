package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

public class ListResourcesResponse extends MCPResponse {
    @Valid
    public ListResourcesResult result;
}
