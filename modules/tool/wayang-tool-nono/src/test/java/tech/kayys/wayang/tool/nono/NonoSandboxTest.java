package tech.kayys.wayang.tool.nono;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the full Nono FFM binding coverage.
 * Some tests are OS-specific (macOS Seatbelt / Linux Landlock).
 */
public class NonoSandboxTest {

    // ── Library loading ───────────────────────────────────────────────────────

    @Test
    public void testIsSupportedDoesNotCrash() {
        boolean supported = NonoSandbox.isSupported();
        System.out.println("Nono sandbox supported: " + supported);
        // No assertion — just verify it doesn't throw
    }

    @Test
    public void testVersionString() {
        String version = NonoSandbox.version();
        System.out.println("Nono version: " + version);
        assertNotNull(version);
    }

    // ── Capability set creation & close ───────────────────────────────────────

    @Test
    public void testCapabilitySetCreationAndClose() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertNotNull(sandbox);
        }
    }

    // ── Filesystem capabilities ───────────────────────────────────────────────

    @Test
    public void testAllowPath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertDoesNotThrow(() -> sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE));
        }
    }

    @Test
    public void testAllowFile() {
        // Use a file that definitely exists
        String javaExe = System.getProperty("java.home") + "/bin/java";
        try (NonoSandbox sandbox = new NonoSandbox()) {
            // If the file doesn't exist (e.g., Windows), just skip
            java.nio.file.Path p = java.nio.file.Paths.get(javaExe);
            if (java.nio.file.Files.exists(p)) {
                assertDoesNotThrow(() -> sandbox.allowFile(javaExe, NonoAccessMode.READ));
            }
        }
    }

    @Test
    public void testFsCapabilityCount() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertEquals(0L, sandbox.getFsCapabilityCount());
            sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
            assertEquals(1L, sandbox.getFsCapabilityCount());
        }
    }

    @Test
    public void testIsPathCovered() {
        String tmpDir = System.getProperty("user.dir"); // Canonical absolute path
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
            assertTrue(sandbox.isPathCovered(tmpDir));
        }
    }

    @Test
    public void testDeduplicate() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
            sandbox.allowPath(tmpDir, NonoAccessMode.READ);
            assertDoesNotThrow(sandbox::deduplicate);
        }
    }

    // ── Network capabilities ──────────────────────────────────────────────────

    @Test
    public void testSetNetworkMode() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertDoesNotThrow(() -> sandbox.setNetworkMode(NonoNetworkMode.BLOCKED));
            assertDoesNotThrow(() -> sandbox.setNetworkMode(NonoNetworkMode.ALLOW_ALL));
        }
    }

    @Test
    public void testGetNetworkMode() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.setNetworkMode(NonoNetworkMode.BLOCKED);
            NonoNetworkMode mode = sandbox.getNetworkMode();
            assertNotNull(mode);
        }
    }

    @Test
    public void testSetNetworkBlocked() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertDoesNotThrow(() -> sandbox.setNetworkBlocked(true));
        }
    }

    @Test
    public void testIsNetworkBlocked() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.setNetworkBlocked(true);
            // Reading it back should not throw
            assertDoesNotThrow(sandbox::isNetworkBlocked);
        }
    }

    @Test
    public void testProxyPort() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.setNetworkMode(NonoNetworkMode.PROXY_ONLY);
            assertDoesNotThrow(() -> sandbox.setProxyPort((short) 8080));
        }
    }

    // ── Command capabilities ──────────────────────────────────────────────────

    @Test
    public void testAllowCommand() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertDoesNotThrow(() -> sandbox.allowCommand("bash"));
        }
    }

    @Test
    public void testBlockCommand() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertDoesNotThrow(() -> sandbox.blockCommand("curl"));
            assertDoesNotThrow(() -> sandbox.blockCommand("wget"));
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    @Test
    public void testGetSummary() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
            String summary = sandbox.getSummary();
            assertNotNull(summary);
            System.out.println("Sandbox summary:\n" + summary);
        }
    }

    // ── Query context ─────────────────────────────────────────────────────────

    @Test
    public void testIsPathAllowed() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
            // Querying is safe — does NOT apply the sandbox
            boolean allowed = sandbox.isPathAllowed(tmpDir, NonoAccessMode.READ);
            System.out.println("Is " + tmpDir + " allowed for READ? " + allowed);
            assertTrue(allowed);
        }
    }

    @Test
    public void testIsNetworkAllowed() {
        try (NonoSandbox sandbox = new NonoSandbox()) {
            sandbox.setNetworkMode(NonoNetworkMode.ALLOW_ALL);
            // Just verify it doesn't crash; actual value depends on platform defaults
            assertDoesNotThrow(sandbox::isNetworkAllowed);
        }
    }

    // ── State serialization ───────────────────────────────────────────────────

    @Test
    public void testToJsonRoundtrip() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        try (NonoSandbox original = new NonoSandbox()) {
            original.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
            original.setNetworkMode(NonoNetworkMode.BLOCKED);

            String json = original.toJson();
            assertNotNull(json);
            assertFalse(json.isBlank());
            System.out.println("Sandbox JSON: " + json);

            // Round-trip: restore from JSON
            try (NonoSandbox restored = NonoSandbox.fromJson(json)) {
                assertNotNull(restored);
                // The restored sandbox should have the same fs count
                assertEquals(original.getFsCapabilityCount(), restored.getFsCapabilityCount());
            }
        }
    }

    // ── Subprocess isolation (non-apply) ──────────────────────────────────────

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void testNonoProcessExecutorRunsCommand() throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");

        NonoSandboxConfig config = new NonoSandboxConfig()
            .addAllowedPath(tmpDir, "READ_WRITE")
            // Allow common system tools needed for echo
            .addAllowedPath("/usr", "READ")
            .addAllowedPath("/bin", "READ")
            .withNetworkMode(NonoNetworkMode.BLOCKED);

        NonoProcessExecutor executor = new NonoProcessExecutor(config);
        var result = executor.execute("echo hello-from-nono", tmpDir, 15_000);

        System.out.println("Subprocess exit code: " + result.exitCode());
        System.out.println("Subprocess stdout: " + result.stdout());
        System.out.println("Subprocess stderr: " + result.stderr());

        // The command should succeed and produce output
        assertEquals(0, result.exitCode(), "Expected exit code 0, stderr: " + result.stderr());
        assertTrue(result.stdout().contains("hello-from-nono"));
    }
}
