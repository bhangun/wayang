package tech.kayys.wayang.agent.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Configuration model for external Model Context Protocol (MCP) servers.
 * Supports standard JSON MCP formats used by Flutter MCP, Claude Desktop,
 * Cursor, and Antigravity.
 */
public record McpServerConfig(
        String id,
        String url,
        String command,
        List<String> args,
        McpTransportType transportType,
        boolean enabled,
        Map<String, String> headers) {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    public McpServerConfig {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("MCP server id is required");
        }
        args = args == null ? List.of() : List.copyOf(args);
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        transportType = transportType == null ? (command != null ? McpTransportType.STDIO : McpTransportType.HTTP) : transportType;
    }

    public static McpServerConfig http(String id, String url) {
        return new McpServerConfig(id, url, null, List.of(), McpTransportType.HTTP, true, Map.of());
    }

    public static McpServerConfig stdio(String id, String command, List<String> args) {
        return new McpServerConfig(id, null, command, args, McpTransportType.STDIO, true, Map.of());
    }

    /**
     * Parses standard MCP configuration JSON (e.g. Flutter MCP Server, Claude Desktop, Cursor).
     *
     * <pre>
     * {
     *   "mcpServers": {
     *     "flutter": {
     *       "command": "flutter",
     *       "args": ["mcp-server"]
     *     }
     *   }
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static List<McpServerConfig> parseMcpServersJson(String json) throws IOException {
        Map<String, Object> root = MAPPER.readValue(json, MAP_TYPE);
        Object mcpServers = root.getOrDefault("mcpServers", root);
        if (!(mcpServers instanceof Map<?, ?> serverMap)) {
            return List.of();
        }

        List<McpServerConfig> configs = new ArrayList<>();
        for (Map.Entry<?, ?> entry : serverMap.entrySet()) {
            String serverId = String.valueOf(entry.getKey());
            if (entry.getValue() instanceof Map<?, ?> details) {
                configs.add(fromMap(serverId, (Map<String, Object>) details));
            }
        }
        return configs;
    }

    @SuppressWarnings("unchecked")
    public static McpServerConfig fromMap(String serverId, Map<String, Object> map) {
        String url = (String) map.get("url");
        String command = (String) map.get("command");
        List<String> args = map.get("args") instanceof List<?> list
            ? list.stream().map(String::valueOf).toList()
            : List.of();
        
        McpTransportType transport = McpTransportType.HTTP;
        if (command != null && !command.isBlank()) {
            transport = McpTransportType.STDIO;
        } else if (map.containsKey("transport")) {
            try {
                transport = McpTransportType.valueOf(String.valueOf(map.get("transport")).toUpperCase());
            } catch (Exception ignored) {}
        }

        boolean enabled = !Boolean.FALSE.equals(map.get("enabled"));
        Map<String, String> headers = map.get("headers") instanceof Map<?, ?> hMap
            ? (Map<String, String>) hMap
            : Map.of();

        return new McpServerConfig(serverId, url, command, args, transport, enabled, headers);
    }
}
