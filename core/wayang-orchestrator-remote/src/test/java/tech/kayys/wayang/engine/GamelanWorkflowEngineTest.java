package tech.kayys.wayang.engine;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.gamelan.GamelanEngineConfig;
import tech.kayys.wayang.gamelan.GamelanWorkflowEngine;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GamelanWorkflowEngineTest {

    @Inject
    GamelanWorkflowEngine engine;

    @InjectMock
    GamelanEngineConfig config;

    @Test
    void testEngineInitialization() {
        assertNotNull(engine);
    }
}
