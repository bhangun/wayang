package tech.kayys.wayang.mcp.client.runtime.schema;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public class MCPError {
    @NotNull
    public int code;
    
    @NotNull
    public String message;
    
    public JsonNode data;
}
