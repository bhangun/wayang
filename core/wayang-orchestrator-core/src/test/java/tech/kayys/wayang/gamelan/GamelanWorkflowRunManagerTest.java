package tech.kayys.wayang.gamelan;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.sdk.client.WorkflowRunOperations;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.gamelan.sdk.client.WorkflowRunOperations.CreateRunBuilder;
import tech.kayys.gamelan.sdk.client.WorkflowRunOperations.ResumeRunBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GamelanWorkflowRunManagerTest {

    @Mock
    private AbstractGamelanWorkflowEngine engine;

    @Mock
    private GamelanClient client;

    @Mock
    private WorkflowRunOperations runOperations;

    @Mock
    private CreateRunBuilder createBuilder;

    @Mock
    private ResumeRunBuilder resumeBuilder;

    private GamelanWorkflowRunManager runManager;

    @BeforeEach
    void setUp() {
        when(engine.client()).thenReturn(client);
        when(client.runs()).thenReturn(runOperations);
        runManager = new GamelanWorkflowRunManager(engine);
    }

    @Test
    void testCreateRun() {
        String workflowId = "test-workflow";
        Map<String, Object> inputs = Map.of("key", "value");
        RunResponse expectedResponse = mock(RunResponse.class);

        when(runOperations.create(workflowId)).thenReturn(createBuilder);
        when(createBuilder.input(anyString(), any())).thenReturn(createBuilder);
        when(createBuilder.execute()).thenReturn(Uni.createFrom().item(expectedResponse));

        RunResponse response = runManager.createRun(workflowId, inputs).await().indefinitely();

        assertEquals(expectedResponse, response);
        verify(createBuilder).input("key", "value");
        verify(createBuilder).execute();
    }

    @Test
    void testGetRun() {
        String runId = "test-run";
        RunResponse expectedResponse = mock(RunResponse.class);

        when(runOperations.get(runId)).thenReturn(Uni.createFrom().item(expectedResponse));

        RunResponse response = runManager.getRun(runId).await().indefinitely();

        assertEquals(expectedResponse, response);
    }

    @Test
    void testResumeRun() {
        String runId = "test-run";
        String humanTaskId = "test-task";
        Map<String, Object> inputs = Map.of("key", "value");
        RunResponse expectedResponse = mock(RunResponse.class);

        when(runOperations.resume(runId)).thenReturn(resumeBuilder);
        when(resumeBuilder.humanTaskId(anyString())).thenReturn(resumeBuilder);
        when(resumeBuilder.input(anyString(), any())).thenReturn(resumeBuilder);
        when(resumeBuilder.execute()).thenReturn(Uni.createFrom().item(expectedResponse));

        RunResponse response = runManager.resumeRun(runId, humanTaskId, inputs).await().indefinitely();

        assertEquals(expectedResponse, response);
        verify(resumeBuilder).humanTaskId(humanTaskId);
        verify(resumeBuilder).input("key", "value");
        verify(resumeBuilder).execute();
    }

    @Test
    void testCancelRun() {
        String runId = "test-run";
        when(runOperations.cancel(runId)).thenReturn(Uni.createFrom().voidItem());

        runManager.cancelRun(runId, "test-reason").await().indefinitely();

        verify(runOperations).cancel(runId);
    }
}
