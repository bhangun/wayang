package tech.kayys.wayang.mcp.client.runtime.schema.resource;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class ReadResourceResult {
    @NotNull
    public List<ResourceContent> contents;
}
