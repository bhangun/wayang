package tech.kayys.wayang.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class AbstractEipToolTest {

    static class SampleRouterEipTool extends AbstractEipTool {
        public SampleRouterEipTool() {
            super("eip.router", "Sample Router", "Routes messages based on condition", "1.0.0",
                    Map.of("type", "object", "properties", Map.of("route", Map.of("type", "string"))));
        }

        @Override
        public CompletableFuture<ToolResult> executeEip(Map<String, Object> arguments, ToolContext context) {
            String route = (String) arguments.get("route");
            if ("invalid".equals(route)) {
                return CompletableFuture.completedFuture(SimpleToolResult.failure("Invalid route"));
            }
            return CompletableFuture.completedFuture(
                    SimpleToolResult.success(Map.of("target", "dest-" + route, "routed", true))
            );
        }
    }

    @Test
    void testDescriptorAndExecution() throws ExecutionException, InterruptedException {
        SampleRouterEipTool tool = new SampleRouterEipTool();
        ToolDescriptor descriptor = tool.descriptor();

        assertNotNull(descriptor);
        assertEquals("Sample Router", descriptor.name());
        assertNotNull(descriptor.id().asString());
        if (descriptor instanceof SimpleToolDescriptor std) {
            assertEquals("eip.router", std.toolId());
        }
        assertEquals("1.0.0", descriptor.version());
        assertFalse(descriptor.inputSchema().isEmpty());

        // Test successful invocation
        ToolInvocation invocation = SimpleToolInvocation.of("eip.router", Map.of("route", "orders"));
        ToolContext context = SimpleToolContext.empty();

        ToolResult result = tool.execute(invocation, context).get();
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
        assertEquals("dest-orders", result.getOutputs().get("target"));
        assertEquals(true, result.getOutputs().get("routed"));

        // Test failure invocation
        ToolInvocation failInvocation = SimpleToolInvocation.of("eip.router", Map.of("route", "invalid"));
        ToolResult failResult = tool.execute(failInvocation, context).get();
        assertFalse(failResult.isSuccess());
        assertEquals("Invalid route", failResult.getErrorMessage());
    }

    @Test
    void testContextAttributes() {
        ToolContext ctx = SimpleToolContext.of(Map.of("tenantId", "tenant-123"));
        assertTrue(ctx.getAttribute("tenantId").isPresent());
        assertEquals("tenant-123", ctx.getAttribute("tenantId").get());
        assertTrue(ctx.getAttribute("missing").isEmpty());
    }
}
