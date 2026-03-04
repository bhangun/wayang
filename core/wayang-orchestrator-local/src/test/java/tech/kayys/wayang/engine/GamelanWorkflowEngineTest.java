package tech.kayys.wayang.engine;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.gamelan.GamelanEngineConfig;
import tech.kayys.wayang.gamelan.GamelanWorkflowEngine;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService;
import tech.kayys.gamelan.engine.workflow.WorkflowRunManager;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GamelanWorkflowEngineTest {

    @Inject
    GamelanWorkflowEngine engine;

    @InjectMock
    GamelanEngineConfig config;

    @InjectMock
    WorkflowRunManager runManager;

    @InjectMock
    WorkflowDefinitionService definitionService;

    @Test
    void testEngineInitialization() {
        assertNotNull(engine);
    }
}
