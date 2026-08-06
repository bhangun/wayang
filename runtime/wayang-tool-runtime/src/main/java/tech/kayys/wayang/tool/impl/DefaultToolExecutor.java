package tech.kayys.wayang.tool.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.tool.ToolExecutor;
import tech.kayys.wayang.tool.ToolRegistry;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolResult;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.validation.ToolArgumentValidator;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class DefaultToolExecutor implements ToolExecutor {

    @Inject
    ToolRegistry toolRegistry;
    
    private final ToolArgumentValidator validator = new ToolArgumentValidator();

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        return toolRegistry.findByName(invocation.name())
                .map(tool -> {
                    try {
                        validator.validate(tool, invocation.arguments());
                        return tool.execute(invocation, context);
                    } catch (Exception e) {
                        return CompletableFuture.<ToolResult>failedFuture(e);
                    }
                })
                .orElseGet(() -> CompletableFuture.failedFuture(new IllegalArgumentException("Tool not found: " + invocation.name())));
    }
}
