package tech.kayys.wayang.agent.mcp;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

/**
 * Composite MCP Tool Client supporting both HTTP (JSON-RPC/SSE) and STDIO (process-based)
 * MCP server configurations (e.g. Flutter MCP Server, filesystem, database, GitHub MCPs).
 */
@ApplicationScoped
public class CompositeMcpToolClient implements McpToolClient {

    private final HttpMcpToolClient httpClient;
    private final StdioMcpToolClient stdioClient;

    public CompositeMcpToolClient() {
        this(new HttpMcpToolClient(), new StdioMcpToolClient());
    }

    public CompositeMcpToolClient(HttpMcpToolClient httpClient, StdioMcpToolClient stdioClient) {
        this.httpClient = httpClient;
        this.stdioClient = stdioClient;
    }

    @Override
    public Uni<List<McpToolDescriptor>> listTools(McpServerConfig server) {
        if (server.transportType() == McpTransportType.STDIO) {
            return stdioClient.listTools(server);
        }
        return httpClient.listTools(server);
    }

    @Override
    public Uni<McpToolCallResult> callTool(McpToolInvocation invocation) {
        Object transport = invocation.context().get(McpTransportContext.KEY_TRANSPORT_TYPE);
        if (McpTransportType.STDIO.name().equalsIgnoreCase(String.valueOf(transport))) {
            return stdioClient.callTool(invocation);
        }
        return httpClient.callTool(invocation);
    }

    @Override
    public Uni<Void> disconnect(String serverId) {
        return Uni.combine().all().unis(
            httpClient.disconnect(serverId),
            stdioClient.disconnect(serverId)
        ).discardItems();
    }
}
