package tech.kayys.wayang.tool.nono;

import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolExecutor;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link ToolExecutor} decorator that runs the wrapped executor inside a
 * nono-sandboxed <b>child JVM process</b> via {@link NonoProcessExecutor}.
 *
 * <p>The main Wayang platform JVM is <b>never sandboxed</b>.  Sandbox policy
 * is built dynamically from the {@link ToolInvocation} parameters:
 * <ul>
 *   <li>{@code __sandbox_allowed_paths} — {@code List<String>} of paths (READ_WRITE)</li>
 *   <li>{@code __sandbox_blocked_commands} — {@code List<String>} of blocked commands</li>
 *   <li>{@code __sandbox_network_mode} — "BLOCKED" | "ALLOW_ALL" | "PROXY_ONLY"</li>
 *   <li>{@code __sandbox_proxy_port} — integer proxy port (for PROXY_ONLY)</li>
 * </ul>
 * If none of these are present a default policy is applied: network blocked,
 * and only the tool's working directory (if any) is allowed.
 */
public class NonoSandboxToolExecutor implements ToolExecutor {

    private final ToolExecutor delegate;

    public NonoSandboxToolExecutor(ToolExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        // ── Build capability config from invocation params ────────────────────
        NonoSandboxConfig config = buildConfig(invocation, context);

        // ── If sandbox is not supported, warn and delegate directly ───────────
        if (!NonoSandbox.isSupported()) {
            System.err.println("[NonoSandboxToolExecutor] WARNING: nono sandbox not supported " +
                "on this platform. Executing without isolation.");
            return delegate.execute(invocation, context);
        }

        // ── Run in sandboxed subprocess ────────────────────────────────────────
        return CompletableFuture.supplyAsync(() -> {
            try {
                String command = extractCommand(invocation);
                String workingDir = context != null && context.getAttribute("workingDirectory").isPresent()
                    ? context.getAttribute("workingDirectory").get().toString() : null;
                long timeout = extractTimeout(invocation);

                NonoProcessExecutor executor = new NonoProcessExecutor(config);
                var result = executor.execute(command, workingDir, timeout);

                // Wrap in ToolResult — delegate handles final result shaping
                // For now pass through to the delegate with the subprocess output
                return buildToolResult(result);
            } catch (Exception e) {
                return CompletableFuture.<ToolResult>failedFuture(
                    new RuntimeException("NonoSandboxToolExecutor failed: " + e.getMessage(), e)).join();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private NonoSandboxConfig buildConfig(ToolInvocation invocation, ToolContext context) {
        NonoSandboxConfig config = new NonoSandboxConfig();

        // Working directory is always allowed with READ_WRITE
        if (context != null && context.getAttribute("workingDirectory").isPresent()) {
            config.addAllowedPath(context.getAttribute("workingDirectory").get().toString(), "READ_WRITE");
        }

        // Dynamic params from invocation
        var params = invocation.arguments();
        if (params == null) return config;

        // Allowed paths
        Object rawPaths = params.get("__sandbox_allowed_paths");
        if (rawPaths instanceof List<?> paths) {
            for (Object p : paths) {
                if (p instanceof String s) config.addAllowedPath(s, "READ_WRITE");
            }
        }

        // Blocked commands
        Object rawBlocked = params.get("__sandbox_blocked_commands");
        if (rawBlocked instanceof List<?> blocked) {
            for (Object cmd : blocked) {
                if (cmd instanceof String s) config.addBlockedCommand(s);
            }
        }

        // Network mode
        Object rawNet = params.get("__sandbox_network_mode");
        if (rawNet instanceof String netMode) {
            config.withNetworkMode(netMode);
        }

        // Proxy port
        Object rawPort = params.get("__sandbox_proxy_port");
        if (rawPort instanceof Number port) {
            config.setProxyPort(port.intValue());
        }

        return config;
    }

    private String extractCommand(ToolInvocation invocation) {
        var params = invocation.arguments();
        if (params != null) {
            Object cmd = params.get("command");
            if (cmd instanceof String s) return s;
        }
        throw new IllegalArgumentException("ToolInvocation must contain a 'command' parameter");
    }

    private long extractTimeout(ToolInvocation invocation) {
        var params = invocation.arguments();
        if (params != null) {
            Object t = params.get("timeout_seconds");
            if (t instanceof Number n) return n.longValue() * 1000L;
        }
        return 30_000L;
    }

    private ToolResult buildToolResult(tech.kayys.wayang.spi.sandbox.SandboxExecutionResult res) {
        // Return a simple anonymous ToolResult carrying the subprocess output
        String output = res.stdout();
        String errMsg = res.stderr();
        int exitCode = res.exitCode();
        boolean success = res.isSuccess();

        return new ToolResult() {
            @Override public java.util.Map<String, Object> getOutputs() {
                return java.util.Map.of(
                    "stdout", output,
                    "stderr", errMsg,
                    "exitCode", exitCode
                );
            }
            @Override public boolean isSuccess() { return success; }
            @Override public String getErrorMessage() { return success ? null : errMsg; }
            @Override public tech.kayys.wayang.identity.ResourceId id() {
                return tech.kayys.wayang.identity.ResourceId.from(
                    tech.kayys.wayang.extension.Id.random(),
                    new tech.kayys.wayang.resource.ResourceType.Tool()
                );
            }
            @Override public tech.kayys.wayang.resource.ResourceType type() {
                return new tech.kayys.wayang.resource.ResourceType.Tool();
            }
            @Override public tech.kayys.wayang.extension.Metadata metadata() {
                return tech.kayys.wayang.extension.Metadata.builder()
                    .name("NonoSandboxResult")
                    .version(tech.kayys.wayang.extension.Version.parse("1.0.0"))
                    .description("Result of a nono-sandboxed subprocess execution")
                    .build();
            }
        };
    }
}
