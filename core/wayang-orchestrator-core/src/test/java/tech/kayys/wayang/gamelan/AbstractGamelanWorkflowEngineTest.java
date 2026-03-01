package tech.kayys.wayang.gamelan;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.engine.node.NodeDefinition;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionId;
import tech.kayys.gamelan.sdk.client.WorkflowDefinitionBuilder;
import tech.kayys.gamelan.sdk.client.WorkflowDefinitionOperations;
import tech.kayys.wayang.schema.WayangSpec;
import tech.kayys.wayang.schema.canvas.CanvasData;
import tech.kayys.wayang.schema.canvas.CanvasNode;
import tech.kayys.gamelan.engine.run.RunResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractGamelanWorkflowEngineTest {

    @Mock
    private GamelanEngineConfig config;

    @Mock
    private GamelanClient client;

    @Mock
    private WorkflowDefinitionOperations workflowOperations;

    @Mock
    private WorkflowDefinitionBuilder definitionBuilder;

    private TestGamelanWorkflowEngine engine;

    private static class TestGamelanWorkflowEngine extends AbstractGamelanWorkflowEngine {
        public TestGamelanWorkflowEngine(GamelanEngineConfig config, GamelanClient client) {
            super(config);
            this.client = client;
        }
    }

    @BeforeEach
    void setUp() {
        lenient().when(client.workflows()).thenReturn(workflowOperations);
        engine = new TestGamelanWorkflowEngine(config, client);
    }

    @Test
    void testDeployMapping() {
        String name = "Test Workflow";
        WayangSpec spec = new WayangSpec();
        CanvasData canvas = new CanvasData();
        CanvasNode node = new CanvasNode();
        node.id = "node-1";
        node.type = "java-executor";
        node.label = "Node 1";
        canvas.nodes.add(node);
        spec.setCanvas(canvas);
        spec.setSpecVersion("1.2.3");

        WorkflowDefinition expectedDefinition = mock(WorkflowDefinition.class);
        WorkflowDefinitionId defId = WorkflowDefinitionId.of("def-123");
        when(expectedDefinition.id()).thenReturn(defId);

        when(workflowOperations.create(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.description(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.version(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.tenantId(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.addNode(any(NodeDefinition.class))).thenReturn(definitionBuilder);
        when(definitionBuilder.execute()).thenReturn(Uni.createFrom().item(expectedDefinition));

        String resultId = engine.deploy(name, spec).await().indefinitely();

        assertEquals("def-123", resultId);
        verify(workflowOperations).create(name);
        verify(definitionBuilder).version("1.2.3");
        verify(definitionBuilder, times(1)).addNode(any(NodeDefinition.class));
        verify(definitionBuilder).execute();
    }

    @Test
    void testRun() {
        String defId = "def-123";
        Map<String, Object> inputs = Map.of("param1", "value1");

        RunResponse runResponse = mock(RunResponse.class);
        when(runResponse.getRunId()).thenReturn("run-456");

        tech.kayys.gamelan.sdk.client.WorkflowRunOperations runOps = mock(
                tech.kayys.gamelan.sdk.client.WorkflowRunOperations.class);
        tech.kayys.gamelan.sdk.client.CreateRunBuilder createRunBuilder = mock(
                tech.kayys.gamelan.sdk.client.CreateRunBuilder.class);

        when(client.runs()).thenReturn(runOps);
        when(runOps.create(defId)).thenReturn(createRunBuilder);
        when(createRunBuilder.input(anyString(), any())).thenReturn(createRunBuilder);
        when(createRunBuilder.execute()).thenReturn(Uni.createFrom().item(runResponse));

        String result = engine.run(defId, inputs).await().indefinitely();

        assertEquals("run-456", result);
        verify(runOps).create(defId);
        verify(createRunBuilder).input("param1", "value1");
        verify(createRunBuilder).execute();
    }

    @Test
    void testListWorkflows() {
        List<WorkflowDefinition> expectedList = List.of(mock(WorkflowDefinition.class));
        when(workflowOperations.list()).thenReturn(Uni.createFrom().item(expectedList));

        List<WorkflowDefinition> result = engine.listWorkflows().await().indefinitely();

        assertEquals(expectedList, result);
        verify(workflowOperations).list();
    }
}
