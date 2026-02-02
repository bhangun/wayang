package tech.kayys.wayang.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

public class GamelanWorkflowEngineTest {

    private GamelanEngineConfig config;

    @BeforeEach
    void setUp() {
        config = GamelanEngineConfig.builder()
                .endpoint("http://localhost:8080")
                .tenantId("test-tenant")
                .apiKey("test-key")
                .timeout(Duration.ofSeconds(5))
                .build();
    }

    @Test
    void testEngineInitialization() {
        try (GamelanWorkflowEngine engine = new GamelanWorkflowEngine(config)) {
            assertNotNull(engine);
        } catch (Exception e) {
            fail("Engine initialization failed: " + e.getMessage());
        }
    }
}
