package tech.kayys.wayang.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import tech.kayys.wayang.gamelan.GamelanEngineConfig;
import tech.kayys.wayang.gamelan.GamelanWorkflowEngine;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class GamelanWorkflowEngineTest {

    private GamelanEngineConfig config;

    @BeforeEach
    void setUp() {
        config = Mockito.mock(GamelanEngineConfig.class);
        when(config.endpoint()).thenReturn("http://localhost:8080");
        when(config.tenantId()).thenReturn("test-tenant");
        when(config.apiKey()).thenReturn(Optional.of("test-key"));
        when(config.timeout()).thenReturn(Duration.ofSeconds(5));
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
