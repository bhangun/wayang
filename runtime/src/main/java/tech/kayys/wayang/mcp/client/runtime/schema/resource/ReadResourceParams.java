package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import jakarta.validation.constraints.NotNull;

public class ReadResourceParams {
    @NotNull
    public String uri;
}
