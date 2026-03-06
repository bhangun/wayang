package tech.kayys.wayang.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import jakarta.enterprise.inject.Instance;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService;
import tech.kayys.gamelan.engine.workflow.WorkflowRunManager;
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
    @SuppressWarnings("unchecked")
    void testEngineInitialization() {
        Instance<WorkflowRunManager> runManagerInstance = Mockito.mock(Instance.class);
        Instance<WorkflowDefinitionService> definitionServiceInstance = Mockito.mock(Instance.class);
        when(runManagerInstance.isResolvable()).thenReturn(true);
        when(definitionServiceInstance.isResolvable()).thenReturn(true);
        when(runManagerInstance.get()).thenReturn(Mockito.mock(WorkflowRunManager.class));
        when(definitionServiceInstance.get()).thenReturn(Mockito.mock(WorkflowDefinitionService.class));

        try (GamelanWorkflowEngine engine = new GamelanWorkflowEngine(
                config, runManagerInstance, definitionServiceInstance)) {
            assertNotNull(engine);
        } catch (Exception e) {
            fail("Engine initialization failed: " + e.getMessage());
        }
    }
}
