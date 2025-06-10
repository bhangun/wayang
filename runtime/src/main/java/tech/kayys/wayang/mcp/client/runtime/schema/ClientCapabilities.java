package tech.kayys.wayang.mcp.client.runtime.schema;

import tech.kayys.wayang.mcp.client.runtime.schema.resource.ResourcesCapability;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.ToolsCapability;

public class ClientCapabilities {
    public ResourcesCapability resources;
    public ToolsCapability tools;
    public PromptsCapability prompts;
    public SamplingCapability sampling;
    public RootsCapability roots;
}
