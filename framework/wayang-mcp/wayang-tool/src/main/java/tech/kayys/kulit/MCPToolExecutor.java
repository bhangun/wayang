package tech.kayys.gollek.mcp.tool;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gollek.mcp.dto.Tool;
import tech.kayys.gollek.mcp.dto.ToolResult;

import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Executes MCP tools with validation, error handling, and observability.
 */
@ApplicationScoped
public class ToolExecutor {

        private static final Logger LOG = Logger.getLogger(ToolExecutor.class);

        @Inject
        MCPClient mcpClient;

        @Inject
        ToolRegistry toolRegistry;

        /**
         * Execute a tool with arguments
         */
        public Uni<ToolResult> executeTool(
                        String toolName,
                        Map<String, Object> arguments) {
                return executeTool(toolName, arguments, Duration.ofSeconds(30));
        }

        /**
         * Execute tool with timeout
         */
        public Uni<ToolResult> executeTool(
                        String toolName,
                        Map<String, Object> arguments,
                        Duration timeout) {
                LOG.debugf("Executing tool: %s with args: %s", toolName, arguments);

                // Validate tool exists
                Optional<Tool> toolOpt = toolRegistry.getTool(toolName);
                if (toolOpt.isEmpty()) {
                        return Uni.createFrom().item(
                                        ToolResult.error(toolName, "Tool not found: " + toolName));
                }

                Tool tool = toolOpt.get();

                // Validate arguments
                if (!tool.validateArguments(arguments)) {
                        return Uni.createFrom().item(
                                        ToolResult.error(toolName, "Invalid arguments for tool: " + toolName));
                }

                // Get connection
                Optional<String> connectionNameOpt = toolRegistry.getConnectionForTool(toolName);
                if (connectionNameOpt.isEmpty()) {
                        return Uni.createFrom().item(
                                        ToolResult.error(toolName, "No connection for tool: " + toolName));
                }

                String connectionName = connectionNameOpt.get();
                Optional<MCPConnection> connectionOpt = mcpClient.getConnection(connectionName);
                if (connectionOpt.isEmpty()) {
                        return Uni.createFrom().item(
                                        ToolResult.error(toolName, "Connection not found: " + connectionName));
                }

                MCPConnection connection = connectionOpt.get();

                // Execute with timeout
                return connection.callTool(toolName, arguments)
                                .ifNoItem().after(timeout).fail()
                                .onItem().transform(response -> convertResponse(toolName, response))
                                .onFailure().recoverWithItem(error -> {
                                        LOG.errorf(error, "Tool execution failed: %s", toolName);
                                        return ToolResult.error(
                                                        toolName,
                                                        "Execution failed: " + error.getMessage());
                                });
        }

        /**
         * Convert MCP response to tool result
         */
        private ToolResult convertResponse(String toolName, MCPResponse response) {
                if (!response.isSuccess()) {
                        String errorMsg = response.getError() != null
                                        ? response.getError().toString()
                                        : "Unknown error";
                        return ToolResult.error(toolName, errorMsg);
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.getResult();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> contentList = (List<Map<String, Object>>) result.get("content");

                if (contentList == null || contentList.isEmpty()) {
                        return ToolResult.success(toolName, "");
                }

                var builder = ToolResult.builder()
                                .toolName(toolName)
                                .success(true);

                contentList.forEach(contentData -> {
                        String type = (String) contentData.get("type");
                        String text = (String) contentData.get("text");
                        String data = (String) contentData.get("data");
                        String mimeType = (String) contentData.get("mimeType");
                        String uri = (String) contentData.get("uri");

                        builder.addContent(
                                        new ToolResult.Content(type, text, data, mimeType, uri));
                });

                return builder.build();
        }

        /**
         * Batch execute multiple tools
         */
        public Uni<Map<String, ToolResult>> executeTools(
                        Map<String, Map<String, Object>> toolCalls) {
                return Uni.combine().all().unis(
                                toolCalls.entrySet().stream()
                                                .map(entry -> executeTool(entry.getKey(), entry.getValue())
                                                                .onItem()
                                                                .transform(result -> Map.entry(entry.getKey(), result)))
                                                .toList())
                                .with(results -> {
                                        return results.stream()
                                                        .collect(java.util.stream.Collectors.toMap(
                                                                        Map.Entry::getKey,
                                                                        Map.Entry::getValue));
                                });
        }
}