package tech.kayys.wayang.tool.nono;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import tech.kayys.wayang.spi.sandbox.Sandbox;
import tech.kayys.wayang.spi.sandbox.SandboxExecutionResult;

/**
 * Java FFM bindings for the nono capability-based sandbox library.
 *
 * <p>Wraps the full nono C FFI API including filesystem capabilities,
 * network policies, command filtering, state serialization, and query contexts.
 *
 * <p><b>IMPORTANT:</b> {@link #apply()} is IRREVERSIBLE for the current OS process.
 * Use {@link NonoProcessExecutor} to sandbox a child JVM process instead.
 */
public class NonoSandbox implements Sandbox {

    // ── Native library bootstrap ──────────────────────────────────────────────

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup STDLIB = LINKER.defaultLookup();
    private static SymbolLookup nonoLib;

    // ── Core ──────────────────────────────────────────────────────────────────
    private static MethodHandle nono_sandbox_is_supported;
    private static MethodHandle nono_last_error;
    private static MethodHandle nono_clear_error;
    private static MethodHandle nono_string_free;
    private static MethodHandle nono_version;

    // ── Capability set ────────────────────────────────────────────────────────
    private static MethodHandle nono_capability_set_new;
    private static MethodHandle nono_capability_set_free;
    private static MethodHandle nono_capability_set_allow_path;
    private static MethodHandle nono_capability_set_allow_file;
    private static MethodHandle nono_capability_set_set_network_blocked;
    private static MethodHandle nono_capability_set_set_network_mode;
    private static MethodHandle nono_capability_set_network_mode;
    private static MethodHandle nono_capability_set_set_proxy_port;
    private static MethodHandle nono_capability_set_proxy_port;
    private static MethodHandle nono_capability_set_allow_command;
    private static MethodHandle nono_capability_set_block_command;
    private static MethodHandle nono_capability_set_deduplicate;
    private static MethodHandle nono_capability_set_path_covered;
    private static MethodHandle nono_capability_set_is_network_blocked;
    private static MethodHandle nono_capability_set_summary;
    private static MethodHandle nono_capability_set_fs_count;
    private static MethodHandle nono_capability_set_fs_original;
    private static MethodHandle nono_capability_set_fs_resolved;
    private static MethodHandle nono_capability_set_fs_access;
    private static MethodHandle nono_capability_set_fs_is_file;

    // ── Query context ─────────────────────────────────────────────────────────
    private static MethodHandle nono_query_context_new;
    private static MethodHandle nono_query_context_free;
    private static MethodHandle nono_query_context_query_path;
    private static MethodHandle nono_query_context_query_network;

    // ── Sandbox apply ─────────────────────────────────────────────────────────
    private static MethodHandle nono_sandbox_apply;
    private static MethodHandle nono_sandbox_support_info;

    // ── State serialization ───────────────────────────────────────────────────
    private static MethodHandle nono_sandbox_state_from_caps;
    private static MethodHandle nono_sandbox_state_free;
    private static MethodHandle nono_sandbox_state_to_json;
    private static MethodHandle nono_sandbox_state_from_json;
    private static MethodHandle nono_sandbox_state_to_caps;

    // NonoQueryResult struct layout: {int status, int reason, addr granted_path, addr access, addr granted, addr requested}
    private static final MemoryLayout QUERY_RESULT_LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("status"),
        ValueLayout.JAVA_INT.withName("reason"),
        ValueLayout.ADDRESS.withName("granted_path"),
        ValueLayout.ADDRESS.withName("access"),
        ValueLayout.ADDRESS.withName("granted"),
        ValueLayout.ADDRESS.withName("requested")
    );

    private static final int QUERY_STATUS_ALLOWED = 0;

    static {
        try {
            System.loadLibrary("nono_ffi");
            nonoLib = SymbolLookup.loaderLookup();
            initMethodHandles();
        } catch (UnsatisfiedLinkError e) {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                String ext = os.contains("mac") ? ".dylib" : (os.contains("win") ? ".dll" : ".so");
                String baseDir = System.getProperty("nono.cwd", System.getProperty("user.dir"));
                // Try target/classes (Maven build copies it there)
                Path libPath = Paths.get(baseDir, "target/classes", "libnono_ffi" + ext).toAbsolutePath();
                if (!Files.exists(libPath)) {
                    libPath = Paths.get(baseDir, "libnono_ffi" + ext).toAbsolutePath();
                }
                System.load(libPath.toString());
                nonoLib = SymbolLookup.loaderLookup();
                initMethodHandles();
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("[wayang-nono] WARNING: nono_ffi native library not found. " +
                    "Ensure it is built (cargo build --release) and on java.library.path.");
            }
        }
    }

    private static void initMethodHandles() {
        // Core
        nono_sandbox_is_supported = lookup("nono_sandbox_is_supported",
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN));
        nono_last_error = lookup("nono_last_error",
            FunctionDescriptor.of(ValueLayout.ADDRESS));
        nono_clear_error = lookup("nono_clear_error",
            FunctionDescriptor.ofVoid());
        nono_string_free = lookup("nono_string_free",
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        nono_version = lookup("nono_version",
            FunctionDescriptor.of(ValueLayout.ADDRESS));

        // Capability set
        nono_capability_set_new = lookup("nono_capability_set_new",
            FunctionDescriptor.of(ValueLayout.ADDRESS));
        nono_capability_set_free = lookup("nono_capability_set_free",
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        nono_capability_set_allow_path = lookup("nono_capability_set_allow_path",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        nono_capability_set_allow_file = lookup("nono_capability_set_allow_file",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        nono_capability_set_set_network_blocked = lookup("nono_capability_set_set_network_blocked",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));
        nono_capability_set_set_network_mode = lookup("nono_capability_set_set_network_mode",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        nono_capability_set_network_mode = lookup("nono_capability_set_network_mode",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        nono_capability_set_set_proxy_port = lookup("nono_capability_set_set_proxy_port",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT));
        nono_capability_set_proxy_port = lookup("nono_capability_set_proxy_port",
            FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS));
        nono_capability_set_allow_command = lookup("nono_capability_set_allow_command",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_capability_set_block_command = lookup("nono_capability_set_block_command",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_capability_set_deduplicate = lookup("nono_capability_set_deduplicate",
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        nono_capability_set_path_covered = lookup("nono_capability_set_path_covered",
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_capability_set_is_network_blocked = lookup("nono_capability_set_is_network_blocked",
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));
        nono_capability_set_summary = lookup("nono_capability_set_summary",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_capability_set_fs_count = lookup("nono_capability_set_fs_count",
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        nono_capability_set_fs_original = lookup("nono_capability_set_fs_original",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        nono_capability_set_fs_resolved = lookup("nono_capability_set_fs_resolved",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        nono_capability_set_fs_access = lookup("nono_capability_set_fs_access",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        nono_capability_set_fs_is_file = lookup("nono_capability_set_fs_is_file",
            FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));

        // Query context
        nono_query_context_new = lookup("nono_query_context_new",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_query_context_free = lookup("nono_query_context_free",
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        nono_query_context_query_path = lookup("nono_query_context_query_path",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        nono_query_context_query_network = lookup("nono_query_context_query_network",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        // Sandbox apply
        nono_sandbox_apply = lookup("nono_sandbox_apply",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

        // State serialization
        nono_sandbox_state_from_caps = lookup("nono_sandbox_state_from_caps",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_sandbox_state_free = lookup("nono_sandbox_state_free",
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        nono_sandbox_state_to_json = lookup("nono_sandbox_state_to_json",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_sandbox_state_from_json = lookup("nono_sandbox_state_from_json",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        nono_sandbox_state_to_caps = lookup("nono_sandbox_state_to_caps",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }

    private static MethodHandle lookup(String symbol, FunctionDescriptor descriptor) {
        Optional<MemorySegment> segment = nonoLib.find(symbol).or(() -> STDLIB.find(symbol));
        if (segment.isPresent()) {
            return LINKER.downcallHandle(segment.get(), descriptor);
        }
        throw new NonoException("Symbol not found in nono_ffi: " + symbol);
    }

    // ── Instance state ────────────────────────────────────────────────────────

    private MemorySegment capabilitySet = MemorySegment.NULL;
    private final Arena arena;

    /**
     * Creates a new sandbox with an empty capability set.
     */
    public NonoSandbox() {
        if (nono_capability_set_new == null) {
            throw new NonoException("nono_ffi library is not loaded.");
        }
        this.arena = Arena.ofConfined();
        try {
            this.capabilitySet = (MemorySegment) nono_capability_set_new.invoke();
        } catch (Throwable e) {
            arena.close();
            throw new NonoException("Failed to initialize capability set", e);
        }
    }

    /** Private constructor for fromJson / deserialization path. */
    private NonoSandbox(MemorySegment existingCaps) {
        if (existingCaps.equals(MemorySegment.NULL)) {
            throw new NonoException("Cannot create NonoSandbox from NULL capability set");
        }
        this.arena = Arena.ofConfined();
        this.capabilitySet = existingCaps;
    }

    // ── Static helpers ────────────────────────────────────────────────────────

    /** Returns true if sandboxing is supported on this platform. */
    public static boolean isSupported() {
        if (nono_sandbox_is_supported == null) return false;
        try {
            return (boolean) nono_sandbox_is_supported.invoke();
        } catch (Throwable e) {
            return false;
        }
    }

    /** Returns the nono library version string. */
    public static String version() {
        if (nono_version == null) return "unknown";
        try (Arena a = Arena.ofConfined()) {
            MemorySegment ptr = (MemorySegment) nono_version.invoke();
            if (ptr.equals(MemorySegment.NULL)) return "unknown";
            String v = ptr.reinterpret(Integer.MAX_VALUE).getString(0);
            nono_string_free.invoke(ptr);
            return v;
        } catch (Throwable e) {
            return "error: " + e.getMessage();
        }
    }

    // ── Filesystem capabilities ───────────────────────────────────────────────

    /**
     * Grants read or read/write access to a directory.
     */
    public void allowPath(String path, NonoAccessMode mode) {
        try {
            MemorySegment pathSeg = arena.allocateFrom(path);
            int rc = (int) nono_capability_set_allow_path.invoke(capabilitySet, pathSeg, mode.getValue());
            if (rc != 0) throw new NonoException("allow_path failed for '" + path + "': " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_allow_path", e);
        }
    }

    /**
     * Grants read or read/write access to a single file.
     */
    public void allowFile(String path, NonoAccessMode mode) {
        try {
            MemorySegment pathSeg = arena.allocateFrom(path);
            int rc = (int) nono_capability_set_allow_file.invoke(capabilitySet, pathSeg, mode.getValue());
            if (rc != 0) throw new NonoException("allow_file failed for '" + path + "': " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_allow_file", e);
        }
    }

    // ── Network capabilities ──────────────────────────────────────────────────

    /**
     * Sets the network mode (BLOCKED, ALLOW_ALL, or PROXY_ONLY).
     */
    public void setNetworkMode(NonoNetworkMode mode) {
        try {
            int rc = (int) nono_capability_set_set_network_mode.invoke(capabilitySet, mode.getValue());
            if (rc != 0) throw new NonoException("set_network_mode failed: " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_set_network_mode", e);
        }
    }

    /**
     * Explicitly blocks or allows all outbound network access.
     */
    public void setNetworkBlocked(boolean blocked) {
        try {
            int rc = (int) nono_capability_set_set_network_blocked.invoke(capabilitySet, blocked);
            if (rc != 0) throw new NonoException("set_network_blocked failed: " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_set_network_blocked", e);
        }
    }

    /**
     * Sets the proxy port when network mode is PROXY_ONLY.
     */
    public void setProxyPort(short port) {
        try {
            int rc = (int) nono_capability_set_set_proxy_port.invoke(capabilitySet, port);
            if (rc != 0) throw new NonoException("set_proxy_port failed: " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_set_proxy_port", e);
        }
    }

    /** Returns the currently configured network mode. */
    public NonoNetworkMode getNetworkMode() {
        try {
            int val = (int) nono_capability_set_network_mode.invoke(capabilitySet);
            return NonoNetworkMode.fromValue(val);
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_network_mode", e);
        }
    }

    /** Returns the currently configured proxy port. */
    public short getProxyPort() {
        try {
            return (short) nono_capability_set_proxy_port.invoke(capabilitySet);
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_proxy_port", e);
        }
    }

    /** Returns whether outbound network access is blocked. */
    public boolean isNetworkBlocked() {
        try {
            return (boolean) nono_capability_set_is_network_blocked.invoke(capabilitySet);
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_is_network_blocked", e);
        }
    }

    // ── Command capabilities ──────────────────────────────────────────────────

    /**
     * Adds a command to the allow-list (overrides any block-list entries).
     */
    public void allowCommand(String cmd) {
        try {
            MemorySegment cmdSeg = arena.allocateFrom(cmd);
            int rc = (int) nono_capability_set_allow_command.invoke(capabilitySet, cmdSeg);
            if (rc != 0) throw new NonoException("allow_command failed for '" + cmd + "': " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_allow_command", e);
        }
    }

    /**
     * Adds a command to the block-list.
     */
    public void blockCommand(String cmd) {
        try {
            MemorySegment cmdSeg = arena.allocateFrom(cmd);
            int rc = (int) nono_capability_set_block_command.invoke(capabilitySet, cmdSeg);
            if (rc != 0) throw new NonoException("block_command failed for '" + cmd + "': " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_block_command", e);
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Deduplicates filesystem capabilities, keeping the highest access level. */
    public void deduplicate() {
        try {
            nono_capability_set_deduplicate.invoke(capabilitySet);
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_deduplicate", e);
        }
    }

    /** Returns whether the given path is covered by an existing directory capability. */
    public boolean isPathCovered(String path) {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment pathSeg = a.allocateFrom(path);
            return (boolean) nono_capability_set_path_covered.invoke(capabilitySet, pathSeg);
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_path_covered", e);
        }
    }

    /** Returns the number of filesystem capabilities in this set. */
    public long getFsCapabilityCount() {
        try {
            return (long) nono_capability_set_fs_count.invoke(capabilitySet);
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_fs_count", e);
        }
    }

    /** Returns a human-readable summary of the capability set. */
    public String getSummary() {
        try {
            MemorySegment ptr = (MemorySegment) nono_capability_set_summary.invoke(capabilitySet);
            if (ptr.equals(MemorySegment.NULL)) return "(empty)";
            String s = ptr.reinterpret(Integer.MAX_VALUE).getString(0);
            nono_string_free.invoke(ptr);
            return s;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_capability_set_summary", e);
        }
    }

    // ── Query context ─────────────────────────────────────────────────────────

    /**
     * Queries whether a specific path/mode combination would be permitted.
     * Does NOT apply the sandbox — safe to call at any time.
     */
    public boolean isPathAllowed(String path, NonoAccessMode mode) {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment ctx = (MemorySegment) nono_query_context_new.invoke(capabilitySet);
            if (ctx.equals(MemorySegment.NULL)) return false;
            try {
                MemorySegment pathSeg = a.allocateFrom(path);
                MemorySegment resultSeg = a.allocate(QUERY_RESULT_LAYOUT);
                int rc = (int) nono_query_context_query_path.invoke(ctx, pathSeg, mode.getValue(), resultSeg);
                if (rc != 0) return false;
                int status = resultSeg.get(ValueLayout.JAVA_INT, 0);
                return status == QUERY_STATUS_ALLOWED;
            } finally {
                nono_query_context_free.invoke(ctx);
            }
        } catch (Throwable e) {
            throw new NonoException("Failed to query path permission", e);
        }
    }

    /**
     * Queries whether outbound network access would be permitted.
     * Does NOT apply the sandbox — safe to call at any time.
     */
    public boolean isNetworkAllowed() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment ctx = (MemorySegment) nono_query_context_new.invoke(capabilitySet);
            if (ctx.equals(MemorySegment.NULL)) return false;
            try {
                MemorySegment resultSeg = a.allocate(QUERY_RESULT_LAYOUT);
                int rc = (int) nono_query_context_query_network.invoke(ctx, resultSeg);
                if (rc != 0) return false;
                int status = resultSeg.get(ValueLayout.JAVA_INT, 0);
                return status == QUERY_STATUS_ALLOWED;
            } finally {
                nono_query_context_free.invoke(ctx);
            }
        } catch (Throwable e) {
            throw new NonoException("Failed to query network permission", e);
        }
    }

    // ── State serialization ───────────────────────────────────────────────────

    /**
     * Serializes the current capability set to a JSON string.
     */
    public String toJson() {
        try {
            MemorySegment state = (MemorySegment) nono_sandbox_state_from_caps.invoke(capabilitySet);
            if (state.equals(MemorySegment.NULL)) {
                throw new NonoException("Failed to create state from caps: " + getLastError());
            }
            try {
                MemorySegment jsonPtr = (MemorySegment) nono_sandbox_state_to_json.invoke(state);
                if (jsonPtr.equals(MemorySegment.NULL)) {
                    throw new NonoException("Failed to serialize state to JSON: " + getLastError());
                }
                String json = jsonPtr.reinterpret(Integer.MAX_VALUE).getString(0);
                nono_string_free.invoke(jsonPtr);
                return json;
            } finally {
                nono_sandbox_state_free.invoke(state);
            }
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to serialize sandbox state to JSON", e);
        }
    }

    /**
     * Deserializes a capability set from a JSON string, returning a new NonoSandbox.
     */
    public static NonoSandbox fromJson(String json) {
        if (nono_sandbox_state_from_json == null) {
            throw new NonoException("nono_ffi library is not loaded.");
        }
        try (Arena a = Arena.ofConfined()) {
            MemorySegment jsonSeg = a.allocateFrom(json);
            MemorySegment state = (MemorySegment) nono_sandbox_state_from_json.invoke(jsonSeg);
            if (state.equals(MemorySegment.NULL)) {
                throw new NonoException("Failed to parse sandbox state JSON: " + getLastErrorStatic());
            }
            try {
                MemorySegment caps = (MemorySegment) nono_sandbox_state_to_caps.invoke(state);
                if (caps.equals(MemorySegment.NULL)) {
                    throw new NonoException("Failed to restore caps from state: " + getLastErrorStatic());
                }
                return new NonoSandbox(caps);
            } finally {
                nono_sandbox_state_free.invoke(state);
            }
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to deserialize sandbox from JSON", e);
        }
    }

    // ── Apply ─────────────────────────────────────────────────────────────────

    /**
     * Applies the sandbox to the current process.
     * <b>IRREVERSIBLE</b> — use {@link NonoProcessExecutor} to sandbox a child process instead.
     */
    public void apply() {
        try {
            int rc = (int) nono_sandbox_apply.invoke(capabilitySet);
            if (rc != 0) throw new NonoException("nono_sandbox_apply failed: " + getLastError());
        } catch (NonoException e) {
            throw e;
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke nono_sandbox_apply", e);
        }
    }

    // ── Error helpers ─────────────────────────────────────────────────────────

    private String getLastError() {
        return getLastErrorStatic();
    }

    private static String getLastErrorStatic() {
        try {
            MemorySegment ptr = (MemorySegment) nono_last_error.invoke();
            if (ptr.equals(MemorySegment.NULL)) return "unknown error";
            String msg = ptr.reinterpret(Integer.MAX_VALUE).getString(0);
            nono_string_free.invoke(ptr);
            return msg;
        } catch (Throwable e) {
            return "error retrieving last error: " + e.getMessage();
        }
    }

    // ── Sandbox SPI impl ──────────────────────────────────────────────────────

    @Override
    public void start() throws Exception {
        apply();
    }

    @Override
    public void stop() throws Exception {
        close();
    }

    @Override
    public void close() {
        if (!capabilitySet.equals(MemorySegment.NULL)) {
            try {
                nono_capability_set_free.invoke(capabilitySet);
            } catch (Throwable ignored) {}
            capabilitySet = MemorySegment.NULL;
        }
        try { arena.close(); } catch (Exception ignored) {}
    }

    @Override
    public SandboxExecutionResult executeCommand(String command, long timeoutMillis) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
        pb.redirectErrorStream(false);
        Process p = pb.start();
        boolean finished = p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            return new SandboxExecutionResult(-1, "", "Command timed out after " + timeoutMillis + "ms");
        }
        String stdout = new String(p.getInputStream().readAllBytes());
        String stderr = new String(p.getErrorStream().readAllBytes());
        return new SandboxExecutionResult(p.exitValue(), stdout, stderr);
    }

    @Override
    public void writeFile(String path, String content) throws Exception {
        Files.writeString(Paths.get(path), content);
    }

    @Override
    public String readFile(String path) throws Exception {
        return Files.readString(Paths.get(path));
    }
}
