package tech.kayys.wayang.mcp.client.runtime.client;

import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

import tech.kayys.wayang.mcp.client.runtime.schema.InitializeResponse;
import tech.kayys.wayang.mcp.client.runtime.schema.ServerCapabilities;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.Prompt;
import tech.kayys.wayang.mcp.client.runtime.schema.prompts.PromptMessage;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.Resource;
import tech.kayys.wayang.mcp.client.runtime.schema.resource.ResourceContent;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.Tool;
import tech.kayys.wayang.mcp.client.runtime.schema.tools.ToolResult;

/**
 * MCP Client Interface
 */
interface MCPClientInterface {
    Mono<InitializeResponse> initialize();
    Mono<List<Resource>> listResources(String cursor);
    Mono<List<ResourceContent>> readResource(String uri);
    Mono<List<Tool>> listTools(String cursor);
    Mono<List<ToolResult>> callTool(String name, Map<String, Object> arguments);
    Mono<List<Prompt>> listPrompts(String cursor);
    Mono<List<PromptMessage>> getPrompt(String name, Map<String, String> arguments);
    ServerCapabilities getServerCapabilities();
    boolean isConnected();
    boolean isInitialized();
    Mono<Void> disconnect();
}
