package tech.kayys.wayang.mcp.client.runtime.schema;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotNull;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.GetPromptRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.ListPromptsRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.ListResourcesRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.ReadResourceRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.CallToolRequest;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.ListToolsRequest;

// Base message types
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InitializeRequest.class, name = "initialize"),
    @JsonSubTypes.Type(value = ListResourcesRequest.class, name = "resources/list"),
    @JsonSubTypes.Type(value = ReadResourceRequest.class, name = "resources/read"),
    @JsonSubTypes.Type(value = ListToolsRequest.class, name = "tools/list"),
    @JsonSubTypes.Type(value = CallToolRequest.class, name = "tools/call"),
    @JsonSubTypes.Type(value = ListPromptsRequest.class, name = "prompts/list"),
    @JsonSubTypes.Type(value = GetPromptRequest.class, name = "prompts/get")
})
public abstract class MCPRequest {
    @NotNull
    public String jsonrpc = "2.0";
    
    @NotNull
    public Object id;
    
    @NotNull
    public abstract String method();
}
