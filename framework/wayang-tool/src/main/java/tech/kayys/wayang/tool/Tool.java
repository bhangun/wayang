package tech.kayys.wayang.tool;

import tech.kayys.wayang.extension.Extension;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.tool.capability.Capability;

public interface Tool extends Extension {
    ToolDescriptor descriptor();
    CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context);

    /**
     * Returns the set of capabilities (e.g., DomainScope, RequiresNetwork) exposed by this tool.
     */
    default Collection<Capability> toolCapabilities() {
        return Collections.emptyList();
    }
}
