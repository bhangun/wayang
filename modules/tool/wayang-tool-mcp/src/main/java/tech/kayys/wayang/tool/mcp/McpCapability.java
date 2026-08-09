package tech.kayys.wayang.tool.mcp;

import tech.kayys.wayang.tool.capability.Capability;

public class McpCapability implements Capability {
    private final String serverId;

    public McpCapability(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public String id() {
        return "mcp";
    }

    @Override
    public boolean satisfies(Capability requested) {
        if (requested instanceof McpCapability reqMcp) {
            // Satisfies if the server IDs match, or if requested server ID is a wildcard "*"
            return reqMcp.serverId.equals("*") || reqMcp.serverId.equals(this.serverId);
        }
        return false;
    }
}
