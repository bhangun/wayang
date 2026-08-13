package tech.kayys.wayang.tool.nono;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Child JVM entry point for sandboxed tool execution.
 *
 * <p>This class is launched as a subprocess by {@link NonoProcessExecutor}.
 * It reads a {@link NonoSandboxConfig.LaunchRequest} JSON from stdin,
 * applies the nono sandbox via the FFM API (irreversibly for this child process),
 * executes the specified command, and writes a {@link NonoSandboxConfig.LaunchResult}
 * JSON to stdout.
 *
 * <p>The parent Wayang JVM is <b>never</b> sandboxed.
 *
 * <p>Exit codes:
 * <ul>
 *   <li>0 — Success (result JSON on stdout)</li>
 *   <li>1 — Fatal error before sandbox or command execution (error JSON on stdout)</li>
 * </ul>
 */
public class NonoSubprocessLauncher {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // Redirect stderr to a separate stream so we don't pollute the JSON result channel (stdout)
        PrintStream originalErr = System.err;

        NonoSandboxConfig.LaunchResult result = new NonoSandboxConfig.LaunchResult();

        try {
            // ── 1. Read launch request from stdin ─────────────────────────────
            InputStream stdin = System.in;
            NonoSandboxConfig.LaunchRequest request = MAPPER.readValue(stdin,
                NonoSandboxConfig.LaunchRequest.class);

            NonoSandboxConfig sandboxCfg = request.getSandbox();

            // ── 2. Configure the Nono sandbox ──────────────────────────────────
            try (NonoSandbox sandbox = new NonoSandbox()) {

                // Filesystem paths
                if (sandboxCfg.getAllowedPaths() != null) {
                    for (NonoSandboxConfig.PathEntry entry : sandboxCfg.getAllowedPaths()) {
                        NonoAccessMode mode = NonoAccessMode.valueOf(entry.getMode().toUpperCase());
                        sandbox.allowPath(entry.getPath(), mode);
                    }
                }

                // Network mode
                if (sandboxCfg.getNetworkMode() != null) {
                    NonoNetworkMode netMode = NonoNetworkMode.valueOf(
                        sandboxCfg.getNetworkMode().toUpperCase());
                    sandbox.setNetworkMode(netMode);
                    if (netMode == NonoNetworkMode.PROXY_ONLY && sandboxCfg.getProxyPort() > 0) {
                        sandbox.setProxyPort((short) sandboxCfg.getProxyPort());
                    }
                }

                // Command allow/block lists
                if (sandboxCfg.getAllowedCommands() != null) {
                    for (String cmd : sandboxCfg.getAllowedCommands()) {
                        sandbox.allowCommand(cmd);
                    }
                }
                if (sandboxCfg.getBlockedCommands() != null) {
                    for (String cmd : sandboxCfg.getBlockedCommands()) {
                        sandbox.blockCommand(cmd);
                    }
                }

                // ── 3. Apply sandbox (IRREVERSIBLE for this child process) ────
                if (NonoSandbox.isSupported()) {
                    sandbox.apply();
                } else {
                    originalErr.println("[NonoSubprocessLauncher] WARNING: " +
                        "Nono sandbox not supported on this platform. Running unsandboxed.");
                }
            }

            // ── 4. Execute the command ─────────────────────────────────────────
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", request.getCommand());
            pb.redirectErrorStream(false);
            if (request.getWorkingDir() != null) {
                pb.directory(Paths.get(request.getWorkingDir()).toFile());
            }

            Process process = pb.start();
            long timeout = request.getTimeoutMillis() > 0 ? request.getTimeoutMillis() : 30_000L;
            boolean finished = process.waitFor(timeout, TimeUnit.MILLISECONDS);

            if (!finished) {
                process.destroyForcibly();
                result.setExitCode(-1);
                result.setError("Command timed out after " + timeout + "ms");
            } else {
                result.setExitCode(process.exitValue());
                result.setStdout(new String(process.getInputStream().readAllBytes()));
                result.setStderr(new String(process.getErrorStream().readAllBytes()));
            }

        } catch (Exception e) {
            result.setExitCode(1);
            result.setError("NonoSubprocessLauncher fatal error: " + e.getMessage());
            originalErr.println("[NonoSubprocessLauncher] Fatal error: " + e);
        }

        // ── 5. Write result JSON to stdout ─────────────────────────────────────
        System.out.println(MAPPER.writeValueAsString(result));
        System.out.flush();
        System.exit(result.getExitCode());
    }
}
