package tech.kayys.wayang.gamelan;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.sdk.client.WorkflowDefinitionOperations;
import tech.kayys.gamelan.sdk.client.WorkflowDefinitionOperations.WorkflowDefinitionBuilder;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.wayang.integration.designer.RouteDesign;
import tech.kayys.wayang.integration.designer.DesignNode;
import tech.kayys.wayang.integration.designer.DesignMetadata;
import tech.kayys.gamelan.engine.node.NodeDefinition;

import java.util.List;
import java.util.Optional;

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
        RouteDesign routeDesign = mock(RouteDesign.class);
        DesignMetadata metadata = mock(DesignMetadata.class);
        DesignNode node = mock(DesignNode.class);
        WorkflowDefinition expectedDefinition = mock(WorkflowDefinition.class);

        when(routeDesign.routeId()).thenReturn("test-route");
        when(routeDesign.name()).thenReturn("Test Route");
        when(routeDesign.description()).thenReturn("Test Description");
        when(routeDesign.tenantId()).thenReturn("test-tenant");
        when(routeDesign.metadata()).thenReturn(metadata);
        when(metadata.version()).thenReturn("1.2.3");
        when(routeDesign.nodes()).thenReturn(List.of(node));

        when(node.nodeId()).thenReturn("node-1");
        when(node.label()).thenReturn("Node 1");
        when(node.nodeType()).thenReturn("java-executor");
        when(node.configuration()).thenReturn(null);

        when(workflowOperations.create(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.description(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.version(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.tenantId(anyString())).thenReturn(definitionBuilder);
        when(definitionBuilder.addNode(any(NodeDefinition.class))).thenReturn(definitionBuilder);
        when(definitionBuilder.execute()).thenReturn(Uni.createFrom().item(expectedDefinition));

        WorkflowDefinition result = engine.deploy(routeDesign).await().indefinitely();

        assertEquals(expectedDefinition, result);
        verify(workflowOperations).create("Test Route");
        verify(definitionBuilder).description("Test Description");
        verify(definitionBuilder).version("1.2.3");
        verify(definitionBuilder).tenantId("test-tenant");
        verify(definitionBuilder, times(1)).addNode(any(NodeDefinition.class));
        verify(definitionBuilder).execute();
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
