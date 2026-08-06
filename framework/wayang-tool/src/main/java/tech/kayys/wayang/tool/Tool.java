package tech.kayys.wayang.tool;

import tech.kayys.wayang.extension.Extension;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Tool extends Extension {
    ToolDescriptor descriptor();
    CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context);
}
