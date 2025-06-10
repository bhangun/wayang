package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

public class ListPromptsResponse extends MCPResponse {
    @Valid
    public ListPromptsResult result;
}
