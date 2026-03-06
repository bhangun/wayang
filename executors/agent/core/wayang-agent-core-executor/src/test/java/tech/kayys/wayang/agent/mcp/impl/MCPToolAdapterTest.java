package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.model.AgentContext;
import tech.kayys.wayang.agent.model.Tool;
import tech.kayys.wayang.agent.model.ToolRegistry;
import tech.kayys.wayang.agent.model.ToolResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolAdapterTest {

    @Test
    void listToolsAndGetToolUseRegistryDefaults() {
        ToolRegistry registry = mock(ToolRegistry.class);
        Tool tool = testTool("web_search");
        when(registry.getAllTools("default")).thenReturn(Uni.createFrom().item(List.of(tool)));
        when(registry.getTool("web_search", "default")).thenReturn(Uni.createFrom().item(tool));

        MCPToolAdapter adapter = new MCPToolAdapter();
        adapter.toolRegistry = registry;

        var tools = adapter.listTools().await().indefinitely();
        var single = adapter.getTool("web_search").await().indefinitely();

        assertEquals(1, tools.size());
        assertEquals("web_search", tools.get(0).getName());
        assertEquals("web_search", single.getName());
        assertEquals("Search the web", single.getDescription());
    }

    @Test
    void executeToolBuildsSuccessResultFromToolOutput() {
        ToolRegistry registry = mock(ToolRegistry.class);
        Tool tool = testTool("router");
        when(registry.getTool("router", "default")).thenReturn(Uni.createFrom().item(tool));

        MCPToolAdapter adapter = new MCPToolAdapter();
        adapter.toolRegistry = registry;

        ToolResult result = adapter.executeTool("router", Map.of("route", "alpha"))
                .await().indefinitely();

        assertTrue(result.success());
        assertEquals("router", result.name());
        assertEquals("done:router", result.output());
        verify(registry).getTool("router", "default");
    }

    @Test
    void validateArgumentsReturnsFalseWhenToolMissing() {
        ToolRegistry registry = mock(ToolRegistry.class);
        when(registry.getTool("missing-tool", "default")).thenReturn(Uni.createFrom().nullItem());

        MCPToolAdapter adapter = new MCPToolAdapter();
        adapter.toolRegistry = registry;

        boolean valid = adapter.validateArguments("missing-tool", Map.of("x", 1))
                .await().indefinitely();

        assertFalse(valid);
    }

    private Tool testTool(String name) {
        return new Tool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return "Search the web";
            }

            @Override
            public Map<String, Object> parameterSchema() {
                return Map.of("type", "object");
            }

            @Override
            public Uni<Boolean> validate(Map<String, Object> arguments) {
                return Uni.createFrom().item(true);
            }

            @Override
            public Uni<String> execute(Map<String, Object> arguments, AgentContext context) {
                return Uni.createFrom().item("done:" + name);
            }
        };
    }
}
