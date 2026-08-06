package tech.kayys.wayang.tool.nono;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NonoSandboxTest {

    @Test
    public void testIsSupportedDoesNotCrash() {
        // Just verify that checking support doesn't crash the JVM
        // and returns a boolean without throwing exceptions.
        boolean supported = NonoSandbox.isSupported();
        System.out.println("Nono Sandbox supported on this platform: " + supported);
    }

    @Test
    public void testCapabilitySetCreation() {
        if (!NonoSandbox.isSupported()) {
            System.out.println("Skipping testCapabilitySetCreation because sandbox is not supported.");
            return;
        }
        
        try (NonoSandbox sandbox = new NonoSandbox()) {
            assertNotNull(sandbox);
            // Don't call sandbox.apply() in a unit test as it restricts the Maven/JVM process.
            String tmpDir = System.getProperty("java.io.tmpdir");
            sandbox.allowPath(tmpDir, NonoAccessMode.READ_WRITE);
        } catch (NonoException e) {
            e.printStackTrace();
            fail("Should not throw an exception when creating and manipulating capability set: " + e.getMessage());
        }
    }
}
