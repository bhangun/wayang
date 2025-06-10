package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class ListResourcesResult {
    @NotNull
    public List<Resource> resources;
    
    public String nextCursor;
}
