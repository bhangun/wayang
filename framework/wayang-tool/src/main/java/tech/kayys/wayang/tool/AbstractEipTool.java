package tech.kayys.wayang.tool;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Base class for Enterprise Integration Pattern (EIP) components operating as Wayang Tools.
 * Enables dual-persona usage:
 * 1. Autonomous AI Agent Function Calling (implements {@link Tool})
 * 2. Visual Low-Code Workflow Node Execution (invoked via arguments map)
 */
public abstract class AbstractEipTool implements Tool {

    private final ToolDescriptor descriptor;

    protected AbstractEipTool(ToolDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor cannot be null");
    }

    protected AbstractEipTool(String name, String description, Map<String, Object> inputSchema) {
        this(SimpleToolDescriptor.of(name, description, inputSchema));
    }

    protected AbstractEipTool(String toolId, String name, String description, String version, Map<String, Object> inputSchema) {
        this(SimpleToolDescriptor.of(toolId, name, description, version, inputSchema));
    }

    @Override
    public ToolDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public ResourceId id() {
        return descriptor.id();
    }

    @Override
    public ResourceType type() {
        return descriptor.type();
    }

    @Override
    public Metadata metadata() {
        return descriptor.metadata();
    }

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        Map<String, Object> args = invocation != null ? invocation.arguments() : Map.of();
        return executeEip(args, context);
    }

    /**
     * Executes the EIP logic asynchronously with input arguments and execution context.
     *
     * @param arguments input payload and configuration parameters
     * @param context   tool execution context
     * @return CompletableFuture containing the ToolResult
     */
    public abstract CompletableFuture<ToolResult> executeEip(Map<String, Object> arguments, ToolContext context);
}
