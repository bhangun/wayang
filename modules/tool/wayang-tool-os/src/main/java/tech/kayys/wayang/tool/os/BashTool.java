package tech.kayys.wayang.tool.os;

import tech.kayys.wayang.tools.spi.Tool;
import tech.kayys.wayang.tools.spi.ToolResult;
import tech.kayys.wayang.tools.spi.ToolContext;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import tech.kayys.wayang.tool.nono.NonoProcessExecutor;
import tech.kayys.wayang.tool.nono.NonoSandboxConfig;
import tech.kayys.wayang.tool.nono.NonoNetworkMode;
import tech.kayys.wayang.spi.sandbox.SandboxExecutionResult;

public final class BashTool implements Tool {

    private static final int DEFAULT_TIMEOUT_SECONDS = 120;
    private static final int MAX_OUTPUT_CHARS = 30_000;

    @Override public String id() { return "bash"; }
    @Override public String name() { return "bash"; }

    @Override public String description() {
        return "Execute a shell command and return its combined stdout/stderr output. " +
               "Runs via `sh -c`. Has a timeout (default 120s, configurable). Use for builds, " +
               "tests, git commands, running scripts, etc.";
    }

    @Override public Map<String, Object> inputSchema() {
        return Schema.object(Schema.props(
                "command", Schema.string("The shell command to run."),
                "timeout_seconds", Schema.integer("Max seconds to wait before killing the command (default 120)."),
                "working_dir", Schema.string("Directory to run the command in (optional).")
        ), "command");
        
    }

    

    @Override public ToolResult execute(Map<String, Object> params, ToolContext context) {
        try {
        String command = (String) params.get("command");
        int timeout = params.containsKey("timeout_seconds") ? ((Number) params.get("timeout_seconds")).intValue() : DEFAULT_TIMEOUT_SECONDS;
        String workingDir = params.containsKey("working_dir") ? (String) params.get("working_dir") : null;

        Path defaultDir = context.workingDirectory() != null ? context.workingDirectory() : Paths.get(System.getProperty("user.dir"));
        String resolvedWorkingDir = workingDir != null ? defaultDir.resolve(workingDir).toString() : defaultDir.toString();

        NonoSandboxConfig config = new NonoSandboxConfig();
        config.addAllowedPath(resolvedWorkingDir, "READ_WRITE");
        
        // Optional user overrides via params
        if (params.containsKey("sandbox_allowed_paths")) {
            for (String p : (Iterable<String>) params.get("sandbox_allowed_paths")) {
                config.addAllowedPath(p, "READ_WRITE");
            }
        }
        if (params.containsKey("sandbox_network_mode")) {
            config.withNetworkMode(params.get("sandbox_network_mode").toString());
        }

        NonoProcessExecutor executor = new NonoProcessExecutor(config);
        SandboxExecutionResult res = executor.execute(command, resolvedWorkingDir, timeout * 1000L);

        String text = trim(res.stdout() + (res.stderr().isEmpty() ? "" : "\n" + res.stderr()));
        String result = "(exit code " + res.exitCode() + ")\n" + text;
        
        return res.isSuccess() ? ToolResult.success(result) : ToolResult.error(result);
    
        } catch (Exception e) {
            return ToolResult.error("Execution failed: " + e.getMessage());
        }
    }

    private String trim(String s) {
        if (s.length() <= MAX_OUTPUT_CHARS) return s;
        return s.substring(0, MAX_OUTPUT_CHARS) + "\n... (output truncated)";
    }
}
