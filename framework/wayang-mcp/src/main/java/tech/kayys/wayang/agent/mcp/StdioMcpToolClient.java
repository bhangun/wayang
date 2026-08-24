package tech.kayys.wayang.agent.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Standard I/O (stdio) process-based MCP client.
 * Spawns sub-processes (such as Flutter MCP server: `flutter mcp-server`,
 * Python fastmcp, or Node @modelcontextprotocol servers) and communicates
 * via JSON-RPC 2.0 over stdin/stdout.
 */
public final class StdioMcpToolClient implements McpToolClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final Map<String, Object> DEFAULT_INPUT_SCHEMA = Map.of("type", "object");

    private final ObjectMapper mapper;
    private final Map<String, StdioProcessSession> activeSessions = new ConcurrentHashMap<>();

    public StdioMcpToolClient() {
        this(new ObjectMapper());
    }

    public StdioMcpToolClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Uni<List<McpToolDescriptor>> listTools(McpServerConfig server) {
        return Uni.createFrom().item(() -> {
            try {
                StdioProcessSession session = getOrCreateSession(server);
                Map<String, Object> request = jsonRpcRequest("tools/list", Map.of());
                Map<String, Object> response = session.sendRequest(request);

                Object result = response.get("result");
                if (result instanceof Map<?, ?> map && map.get("tools") instanceof List<?> list) {
                    return list.stream()
                        .map(McpMaps::fromObject)
                        .filter(raw -> !raw.isEmpty())
                        .map(raw -> toToolDescriptor(server.id(), raw))
                        .toList();
                }
                return List.<McpToolDescriptor>of();
            } catch (Exception e) {
                return List.<McpToolDescriptor>of();
            }
        });
    }

    @Override
    public Uni<McpToolCallResult> callTool(McpToolInvocation invocation) {
        Instant start = Instant.now();
        return Uni.createFrom().item(() -> {
            try {
                String serverId = invocation.serverId();
                StdioProcessSession session = activeSessions.get(serverId);
                if (session == null) {
                    // Try to extract command from invocation context
                    String command = (String) invocation.context().get(McpTransportContext.KEY_COMMAND);
                    @SuppressWarnings("unchecked")
                    List<String> args = (List<String>) invocation.context().getOrDefault(McpTransportContext.KEY_ARGS, List.of());
                    if (command != null && !command.isBlank()) {
                        session = getOrCreateSession(McpServerConfig.stdio(serverId, command, args));
                    }
                }

                if (session == null) {
                    return McpToolCallResult.failure("No active stdio session for MCP server: " + serverId, 0);
                }

                Map<String, Object> request = jsonRpcRequest("tools/call", Map.of(
                    "name", invocation.toolName(),
                    "arguments", invocation.arguments()
                ));

                Map<String, Object> response = session.sendRequest(request);
                long duration = Duration.between(start, Instant.now()).toMillis();

                if (response.containsKey("error")) {
                    return McpToolCallResult.failure(String.valueOf(response.get("error")), duration);
                }

                return McpToolCallResult.success(response.get("result"), duration, Map.of("serverId", serverId, "transport", "stdio"));

            } catch (Exception e) {
                long duration = Duration.between(start, Instant.now()).toMillis();
                return McpToolCallResult.failure("Stdio MCP call failed: " + e.getMessage(), duration);
            }
        });
    }

    @Override
    public Uni<Void> disconnect(String serverId) {
        return Uni.createFrom().item(() -> {
            StdioProcessSession session = activeSessions.remove(serverId);
            if (session != null) {
                session.close();
            }
            return null;
        });
    }

    private synchronized StdioProcessSession getOrCreateSession(McpServerConfig server) throws IOException {
        StdioProcessSession session = activeSessions.get(server.id());
        if (session != null && session.isAlive()) {
            return session;
        }

        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(server.command());
        fullCommand.addAll(server.args());

        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process process = pb.start();

        StdioProcessSession newSession = new StdioProcessSession(process, mapper);
        // Initialize MCP handshake
        try {
            Map<String, Object> initParams = Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of("tools", Map.of()),
                "clientInfo", Map.of("name", "WayangAgent", "version", "1.0.0")
            );
            newSession.sendRequest(jsonRpcRequest("initialize", initParams));
        } catch (Exception ignored) {}

        activeSessions.put(server.id(), newSession);
        return newSession;
    }

    private McpToolDescriptor toToolDescriptor(String serverId, Map<String, Object> raw) {
        String name = String.valueOf(raw.getOrDefault("name", "unknown"));
        String description = String.valueOf(raw.getOrDefault("description", name));
        Map<String, Object> schema = McpMaps.fromObject(raw.get("inputSchema"));
        if (schema.isEmpty()) {
            schema = DEFAULT_INPUT_SCHEMA;
        }
        return new McpToolDescriptor(serverId, name, description, schema, Map.of("transport", "stdio"));
    }

    private Map<String, Object> jsonRpcRequest(String method, Map<String, Object> params) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("jsonrpc", "2.0");
        req.put("id", UUID.randomUUID().toString());
        req.put("method", method);
        if (params != null && !params.isEmpty()) {
            req.put("params", params);
        }
        return req;
    }

    private static final class StdioProcessSession implements AutoCloseable {
        private final Process process;
        private final ObjectMapper mapper;
        private final BufferedWriter writer;
        private final BufferedReader reader;

        StdioProcessSession(Process process, ObjectMapper mapper) {
            this.process = process;
            this.mapper = mapper;
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        }

        boolean isAlive() {
            return process.isAlive();
        }

        synchronized Map<String, Object> sendRequest(Map<String, Object> request) throws IOException {
            String json = mapper.writeValueAsString(request);
            writer.write(json);
            writer.newLine();
            writer.flush();

            String line = reader.readLine();
            if (line == null) {
                throw new IOException("EOF received from stdio MCP process");
            }
            return mapper.readValue(line, MAP_TYPE);
        }

        @Override
        public void close() {
            try {
                writer.close();
            } catch (Exception ignored) {}
            try {
                reader.close();
            } catch (Exception ignored) {}
            process.destroyForcibly();
        }
    }
}
