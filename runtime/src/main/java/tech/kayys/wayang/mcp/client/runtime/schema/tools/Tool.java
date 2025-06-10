package tech.kayys.wayang.mcp.client.runtime.schema.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public class Tool {
    @NotNull
    public String name;
    
    public String description;
    
    @JsonProperty("inputSchema")
    public JsonNode inputSchema; // JSON Schema
}
