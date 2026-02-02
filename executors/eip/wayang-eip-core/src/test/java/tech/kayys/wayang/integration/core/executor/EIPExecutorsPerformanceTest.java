package tech.kayys.wayang.integration.core.executor;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.node.*;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class EIPExecutorsPerformanceTest {

    @Inject
    SplitterExecutor splitterExecutor;

    @Inject
    FilterExecutor filterExecutor;

    @Inject
    TransformerExecutor transformerExecutor;

    @Test
    @Disabled("Run manually for performance testing")
    void testSplitterThroughput() throws Exception {
        // Given
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeList.add(i);
        }

        // When
        long startTime = System.currentTimeMillis();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            NodeExecutionTask task = createTask(Map.of(
                    "message", largeList,
                    "strategy", "fixed",
                    "batchSize", 100));

            splitterExecutor.execute(task)
                    .await().atMost(Duration.ofSeconds(5));
        }

        long duration = System.currentTimeMillis() - startTime;
        double throughput = (iterations * 1000.0) / duration;

        // Then
        System.out.println("Splitter throughput: " + throughput + " ops/sec");
        assertThat(throughput).isGreaterThan(50); // At least 50 ops/sec
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }
}
