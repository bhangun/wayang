package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPResponse;

public class GetPromptResponse extends MCPResponse {
    @Valid
    public GetPromptResult result;
}
