package tech.kayys.wayang.mcp.client.runtime.schema;

import jakarta.validation.Valid;

public class InitializeResponse extends MCPResponse {
    @Valid
    public ServerCapabilities result;
}
