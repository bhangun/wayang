package tech.kayys.wayang.tool.nono;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration POJO for the Nono sandbox subprocess execution.
 * Serialized to JSON and passed to {@link NonoSubprocessLauncher} via stdin.
 */
public class NonoSandboxConfig {

    /** Filesystem path grants. */
    private List<PathEntry> allowedPaths = new ArrayList<>();
    /** Commands explicitly allowed (overrides block list). */
    private List<String> allowedCommands = new ArrayList<>();
    /** Commands explicitly blocked. */
    private List<String> blockedCommands = new ArrayList<>();
    /** Network mode: "BLOCKED", "ALLOW_ALL", or "PROXY_ONLY". */
    private String networkMode = "BLOCKED";
    /** Proxy port when networkMode is PROXY_ONLY. */
    private int proxyPort = 0;

    // ── Getters / setters ─────────────────────────────────────────────────────

    public List<PathEntry> getAllowedPaths() { return allowedPaths; }
    public void setAllowedPaths(List<PathEntry> p) { this.allowedPaths = p; }

    public List<String> getAllowedCommands() { return allowedCommands; }
    public void setAllowedCommands(List<String> c) { this.allowedCommands = c; }

    public List<String> getBlockedCommands() { return blockedCommands; }
    public void setBlockedCommands(List<String> c) { this.blockedCommands = c; }

    public String getNetworkMode() { return networkMode; }
    public void setNetworkMode(String m) { this.networkMode = m; }

    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int p) { this.proxyPort = p; }

    // ── Fluent builders ───────────────────────────────────────────────────────

    public NonoSandboxConfig addAllowedPath(String path, String mode) {
        allowedPaths.add(new PathEntry(path, mode));
        return this;
    }

    public NonoSandboxConfig addBlockedCommand(String cmd) {
        blockedCommands.add(cmd);
        return this;
    }

    public NonoSandboxConfig addAllowedCommand(String cmd) {
        allowedCommands.add(cmd);
        return this;
    }

    public NonoSandboxConfig withNetworkMode(String mode) {
        this.networkMode = mode;
        return this;
    }

    public NonoSandboxConfig withNetworkMode(NonoNetworkMode mode) {
        this.networkMode = mode.name();
        return this;
    }

    // ── Nested POJO ───────────────────────────────────────────────────────────

    public static class PathEntry {
        private String path;
        /** "READ", "WRITE", or "READ_WRITE" */
        private String mode;

        public PathEntry() {}
        public PathEntry(String path, String mode) {
            this.path = path;
            this.mode = mode;
        }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
    }

    // ── Launcher request wrapper ──────────────────────────────────────────────

    /** Full request payload sent to the child process via stdin. */
    public static class LaunchRequest {
        private String command;
        private String workingDir;
        private long timeoutMillis = 30_000;
        private NonoSandboxConfig sandbox = new NonoSandboxConfig();

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }

        public String getWorkingDir() { return workingDir; }
        public void setWorkingDir(String workingDir) { this.workingDir = workingDir; }

        public long getTimeoutMillis() { return timeoutMillis; }
        public void setTimeoutMillis(long t) { this.timeoutMillis = t; }

        public NonoSandboxConfig getSandbox() { return sandbox; }
        public void setSandbox(NonoSandboxConfig sandbox) { this.sandbox = sandbox; }
    }

    /** Response payload returned by the child process on stdout. */
    public static class LaunchResult {
        private int exitCode;
        private String stdout = "";
        private String stderr = "";
        private String error;

        public int getExitCode() { return exitCode; }
        public void setExitCode(int e) { this.exitCode = e; }

        public String getStdout() { return stdout; }
        public void setStdout(String s) { this.stdout = s; }

        public String getStderr() { return stderr; }
        public void setStderr(String s) { this.stderr = s; }

        public String getError() { return error; }
        public void setError(String e) { this.error = e; }

        public boolean isSuccess() { return exitCode == 0 && error == null; }
    }
}
