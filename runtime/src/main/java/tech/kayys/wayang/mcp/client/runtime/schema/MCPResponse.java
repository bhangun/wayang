package tech.kayys.wayang.mcp.client.runtime.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


import jakarta.validation.constraints.NotNull;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.GetPromptResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.ListPromptsResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.ListResourcesResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.ReadResourceResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.CallToolResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.ListToolsResponse;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InitializeResponse.class, name = "initialize"),
    @JsonSubTypes.Type(value = ListResourcesResponse.class, name = "resources/list"),
    @JsonSubTypes.Type(value = ReadResourceResponse.class, name = "resources/read"),
    @JsonSubTypes.Type(value = ListToolsResponse.class, name = "tools/list"),
    @JsonSubTypes.Type(value = CallToolResponse.class, name = "tools/call"),
    @JsonSubTypes.Type(value = ListPromptsResponse.class, name = "prompts/list"),
    @JsonSubTypes.Type(value = GetPromptResponse.class, name = "prompts/get")
})
public abstract class MCPResponse {
    @NotNull
    public String jsonrpc = "2.0";
    
    @NotNull
    public Object id;
    
    @JsonProperty("error")
    public MCPError error;
}
