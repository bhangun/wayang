package tech.kayys.wayang.eip.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.eip.dto.RouterDto;
import tech.kayys.wayang.eip.service.RouteEvaluator;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterExecutorTest {

    @Test
    void executeReturnsFirstSelectedRoute() {
        RouterExecutor executor = new RouterExecutor();
        executor.objectMapper = new ObjectMapper();
        executor.routeEvaluator = new FakeRouteEvaluator(List.of("high-priority", "fallback"));

        NodeExecutionTask task = createTask(Map.of(
                "strategy", "first",
                "defaultRoute", "default-node",
                "rules", List.of(
                        Map.of("condition", "always", "targetNode", "high-priority", "priority", 100))));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("high-priority", result.output().get("route"));
        assertEquals(List.of("high-priority", "fallback"), result.output().get("selectedRoutes"));
        assertTrue(result.output().containsKey("routedAt"));
    }

    @Test
    void executeReturnsEmptyRouteWhenNoSelection() {
        RouterExecutor executor = new RouterExecutor();
        executor.objectMapper = new ObjectMapper();
        executor.routeEvaluator = new FakeRouteEvaluator(List.of());

        NodeExecutionTask task = createTask(Map.of(
                "strategy", "first",
                "defaultRoute", "default-node",
                "rules", List.of(
                        Map.of("condition", "status == 'missing'", "targetNode", "default-node", "priority", 1))));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("", result.output().get("route"));
        assertEquals(List.of(), result.output().get("selectedRoutes"));
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("router-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class FakeRouteEvaluator extends RouteEvaluator {
        private final List<String> routes;

        private FakeRouteEvaluator(List<String> routes) {
            this.routes = routes;
        }

        @Override
        public Uni<List<String>> evaluateRoutes(RouterDto config, Map<String, Object> context) {
            return Uni.createFrom().item(routes);
        }
    }
}
