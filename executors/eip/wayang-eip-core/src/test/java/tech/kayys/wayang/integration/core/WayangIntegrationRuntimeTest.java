package tech.kayys.wayang.integration.core;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.sdk.executor.RemoteExecutorRuntime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class WayangIntegrationRuntimeTest {

    @Inject
    WayangIntegrationRuntime runtime;

    @Test
    void testRuntimeInitialization() {
        assertNotNull(runtime, "Runtime should be injectable");
        assertTrue(runtime instanceof RemoteExecutorRuntime, "Should duplicate RemoteExecutorRuntime behavior");
    }
}
