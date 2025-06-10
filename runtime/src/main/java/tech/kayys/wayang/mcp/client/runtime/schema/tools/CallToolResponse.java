package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

public class CallToolResponse extends MCPResponse {
    @Valid
    public CallToolResult result;
}
