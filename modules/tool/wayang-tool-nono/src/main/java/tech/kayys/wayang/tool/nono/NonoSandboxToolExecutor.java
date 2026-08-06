package tech.kayys.wayang.tool.nono;

import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolExecutor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.util.concurrent.CompletableFuture;

/**
 * A ToolExecutor that applies the Nono Sandbox before delegating to the actual ToolExecutor.
 * WARNING: Applying the sandbox is an irreversible operation for the current process/thread 
 * depending on the OS capabilities (Landlock/Seatbelt).
 */
public class NonoSandboxToolExecutor implements ToolExecutor {

    private final ToolExecutor delegate;
    private final NonoSandbox sandbox;

    public NonoSandboxToolExecutor(ToolExecutor delegate, NonoSandbox sandbox) {
        this.delegate = delegate;
        this.sandbox = sandbox;
    }

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        try {
            // Apply the sandbox rules
            if (NonoSandbox.isSupported()) {
                sandbox.apply();
            } else {
                // If not supported, we can either fail or proceed depending on strictness.
                // For now, we proceed with a warning or just rely on the delegate.
                System.err.println("WARNING: Nono Sandbox is not supported on this platform. Execution will not be sandboxed.");
            }
        } catch (NonoException e) {
            return CompletableFuture.failedFuture(new RuntimeException("Failed to apply sandbox: " + e.getMessage(), e));
        }

        // Delegate to the actual tool execution
        return delegate.execute(invocation, context);
    }
}
