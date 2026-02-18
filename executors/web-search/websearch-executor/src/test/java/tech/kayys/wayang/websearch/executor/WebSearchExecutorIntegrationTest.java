package tech.kayys.wayang.websearch.executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import tech.kayys.gamelan.engine.node.*;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WebSearchExecutorIntegrationTest {

    private WebSearchExecutor webSearchExecutor;

    @BeforeEach
    void setUp() {
        webSearchExecutor = new WebSearchExecutor();
    }

    @Test
    void testWebSearchExecutorBasicFunctionality() {
        // Test executor identification methods
        assertEquals("web-search", webSearchExecutor.getExecutorType());
        assertArrayEquals(new String[]{"web-search", "search", "web-search-node"}, 
                         webSearchExecutor.getSupportedNodeTypes());
    }

    @Test
    void testCanHandleMethod() {
        // Create a mock task
        NodeId nodeId = new NodeId("test-node-" + UUID.randomUUID());
        WorkflowRunId runId = new WorkflowRunId("test-run-" + UUID.randomUUID());
        
        NodeExecutionTask task = new NodeExecutionTask(
                runId,
                nodeId,
                1,
                null, // token
                Map.of("query", "test query"),
                null // retry policy
        );

        // Test that the executor can handle the task (default implementation returns true)
        assertTrue(webSearchExecutor.canHandle(task));
    }

    @Test
    void testMaxConcurrentTasks() {
        // Test the default max concurrent tasks value
        assertEquals(Integer.MAX_VALUE, webSearchExecutor.getMaxConcurrentTasks());
    }

    @Test
    void testReadyStatus() {
        // Test the default ready status
        assertTrue(webSearchExecutor.isReady());
    }
}