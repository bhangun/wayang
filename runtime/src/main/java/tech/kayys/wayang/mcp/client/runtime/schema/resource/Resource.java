package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public class Resource {
    @NotNull
    public String uri;
    
    @NotNull
    public String name;
    
    public String description;
    
    public String mimeType;
    
    public Map<String, Object> annotations;
}
