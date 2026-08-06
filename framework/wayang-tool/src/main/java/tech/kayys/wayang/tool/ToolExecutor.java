package tech.kayys.wayang.tool;

import java.util.concurrent.CompletableFuture;

public interface ToolExecutor {
    CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context);
}
