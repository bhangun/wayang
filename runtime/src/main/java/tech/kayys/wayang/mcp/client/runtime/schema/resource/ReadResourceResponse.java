package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

public class ReadResourceResponse extends MCPResponse {
    @Valid
    public ReadResourceResult result;
}
