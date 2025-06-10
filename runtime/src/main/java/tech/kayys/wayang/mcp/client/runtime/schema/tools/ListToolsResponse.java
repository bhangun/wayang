package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

public class ListToolsResponse extends MCPResponse {
    @Valid
    public ListToolsResult result;
}
