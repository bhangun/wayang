package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.Valid;
import tech.kayys.wayang.mcp.client.runtime.schema.MCPRequest;

public class GetPromptRequest extends MCPRequest {
    @Valid
    public GetPromptParams params;
    
    @Override
    public String method() { return "prompts/get"; }
}
