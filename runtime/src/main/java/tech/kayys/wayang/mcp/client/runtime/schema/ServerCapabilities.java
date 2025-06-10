package tech.kayys.wayang.mcp.client.runtime.schema;

import tech.kayys.wayang.mcp.client.runtime.schema.resource.ResourcesCapability;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.ToolsCapability;

public class ServerCapabilities {
    public ResourcesCapability resources;
    public ToolsCapability tools;
    public PromptsCapability prompts;
    public LoggingCapability logging;
    public ExperimentalCapabilities experimental;
}
