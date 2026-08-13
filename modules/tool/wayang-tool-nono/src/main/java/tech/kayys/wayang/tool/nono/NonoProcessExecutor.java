package tech.kayys.wayang.tool.nono;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tech.kayys.wayang.spi.sandbox.SandboxExecutionResult;

/**
 * Executes a shell command inside a child JVM process sandboxed by Nono.
 *
 * <p>The main Wayang platform JVM is <b>never</b> sandboxed.
 * This executor forks a child JVM running {@link NonoSubprocessLauncher} as the main class,
 * sends the sandbox config + command via stdin, and reads the JSON result from stdout.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * NonoSandboxConfig config = new NonoSandboxConfig()
 *     .addAllowedPath("/tmp/agent-work", "READ_WRITE")
 *     .addBlockedCommand("curl")
 *     .withNetworkMode(NonoNetworkMode.BLOCKED);
 *
 * NonoProcessExecutor executor = new NonoProcessExecutor(config);
 * SandboxExecutionResult result = executor.execute("bash -c 'ls /tmp/agent-work'",
 *     "/tmp/agent-work", 30_000);
 * }</pre>
 */
public class NonoProcessExecutor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NonoSandboxConfig sandboxConfig;

    public NonoProcessExecutor(NonoSandboxConfig sandboxConfig) {
        this.sandboxConfig = sandboxConfig;
    }

    /**
     * Executes a command inside a sandboxed child JVM.
     *
     * @param command       Shell command to run (e.g. {@code "bash -c 'echo hello'"})
     * @param workingDir    Working directory for the command (may be null)
     * @param timeoutMillis Maximum time to wait for the child JVM to complete
     * @return execution result containing exit code, stdout, and stderr
     * @throws Exception if the child process could not be launched
     */
    public SandboxExecutionResult execute(String command, String workingDir, long timeoutMillis)
        throws Exception {

        // Build the request payload
        NonoSandboxConfig.LaunchRequest request = new NonoSandboxConfig.LaunchRequest();
        request.setCommand(command);
        request.setWorkingDir(workingDir);
        request.setTimeoutMillis(timeoutMillis);
        request.setSandbox(sandboxConfig);

        String jsonPayload = MAPPER.writeValueAsString(request);

        // Locate the current JVM executable
        String javaExe = ProcessHandle.current().info().command()
            .orElse(Paths.get(System.getProperty("java.home"), "bin", "java").toString());

        // Build classpath from current process
        String classpath = System.getProperty("java.class.path");

        // Build the child JVM command
        List<String> commandList = new ArrayList<>();
        commandList.add(javaExe);
        commandList.add("-cp");
        commandList.add(classpath);
        commandList.add("--enable-native-access=ALL-UNNAMED");

        String libPath = System.getProperty("java.library.path");
        if (libPath != null && !libPath.isBlank()) {
            commandList.add("-Djava.library.path=" + libPath);
        }
        
        commandList.add("-Dnono.cwd=" + System.getProperty("user.dir"));

        commandList.add(NonoSubprocessLauncher.class.getName());

        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.redirectErrorStream(false);
        if (workingDir != null) {
            pb.directory(Paths.get(workingDir).toFile());
        }

        Process child = pb.start();

        // Send JSON payload to stdin and close it
        try (OutputStream stdin = child.getOutputStream()) {
            stdin.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        // Wait for the child to complete
        boolean finished = child.waitFor(timeoutMillis + 5_000, TimeUnit.MILLISECONDS);
        if (!finished) {
            child.destroyForcibly();
            return new SandboxExecutionResult(-1, "",
                "Sandboxed subprocess timed out after " + timeoutMillis + "ms");
        }

        // Read result JSON from stdout
        byte[] rawOut = child.getInputStream().readAllBytes();
        byte[] rawErr = child.getErrorStream().readAllBytes();

        if (rawOut.length == 0) {
            String errMsg = rawErr.length > 0 ? new String(rawErr, StandardCharsets.UTF_8)
                : "(no output from child process)";
            return new SandboxExecutionResult(child.exitValue(), "", errMsg);
        }

        try {
            NonoSandboxConfig.LaunchResult result = MAPPER.readValue(rawOut,
                NonoSandboxConfig.LaunchResult.class);
            return new SandboxExecutionResult(
                result.getExitCode(),
                result.getStdout(),
                result.isSuccess() ? result.getStderr()
                    : (result.getError() != null ? result.getError() : result.getStderr())
            );
        } catch (Exception parseEx) {
            // Child may have printed raw output instead of JSON on fatal error
            return new SandboxExecutionResult(child.exitValue(),
                new String(rawOut, StandardCharsets.UTF_8),
                new String(rawErr, StandardCharsets.UTF_8));
        }
    }
}
