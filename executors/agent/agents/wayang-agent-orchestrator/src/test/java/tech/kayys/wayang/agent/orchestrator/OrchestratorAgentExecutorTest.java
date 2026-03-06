package tech.kayys.wayang.agent.orchestrator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import tech.kayys.gamelan.engine.node.DefaultNodeExecutionResult;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionStatus;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.wayang.agent.analytic.AnalyticAgentExecutor;
import tech.kayys.wayang.agent.coder.CoderAgentExecutor;
import tech.kayys.wayang.agent.common.CommonAgentExecutor;
import tech.kayys.wayang.agent.core.inference.AgentInferenceRequest;
import tech.kayys.wayang.agent.core.inference.AgentInferenceResponse;
import tech.kayys.wayang.agent.core.inference.GollekInferenceService;
import tech.kayys.wayang.agent.evaluator.EvaluatorAgentExecutor;
import tech.kayys.wayang.agent.planner.PlannerAgentExecutor;
import tech.kayys.wayang.agent.orchestrator.prompts.OrchestrationPrompts;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrchestratorAgentExecutorTest {

    @Test
    void executeObjectivePathReturnsCompletedDecision() {
        TestOrchestratorAgentExecutor executor = new TestOrchestratorAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.orchestrationPrompts = new OrchestrationPrompts();
        executor.commonAgentExecutor = new FakeCommonAgentExecutor();
        executor.plannerAgentExecutor = new FakePlannerAgentExecutor();
        executor.coderAgentExecutor = new FakeCoderAgentExecutor();
        executor.analyticAgentExecutor = new FakeAnalyticAgentExecutor();
        executor.evaluatorAgentExecutor = new FakeEvaluatorAgentExecutor();
        FakeGollekInferenceService inferenceService = new FakeGollekInferenceService(
                AgentInferenceResponse.builder()
                        .content("delegate to planner-agent then coder-agent")
                        .providerUsed("tech.kayys/openai-provider")
                        .modelUsed("gpt-4o")
                        .totalTokens(77)
                        .latency(Duration.ofMillis(90))
                        .build());
        executor.inferenceService = inferenceService;

        Map<String, Object> context = Map.of(
                "objective", "Build API then test it",
                "taskType", "DELEGATE");
        NodeExecutionTask task = createTask(context);

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("delegate to planner-agent then coder-agent", result.output().get("decision"));
        assertEquals("DELEGATE", result.output().get("taskType"));
        assertEquals("tech.kayys/openai-provider", inferenceService.lastRequest.getPreferredProvider());
        assertEquals(context, inferenceService.lastRequest.getAdditionalParams().get("context"));
    }

    @Test
    void executeSequentialAgentLoopIncludesEvaluatorAndReplan() {
        TestOrchestratorAgentExecutor executor = new TestOrchestratorAgentExecutor();
        executor.setObjectMapper(lenientMapper());
        executor.commonAgentExecutor = new FakeCommonAgentExecutor();
        executor.plannerAgentExecutor = new FakePlannerAgentExecutor();
        executor.coderAgentExecutor = new FakeCoderAgentExecutor();
        executor.analyticAgentExecutor = new FakeAnalyticAgentExecutor();
        executor.evaluatorAgentExecutor = new FakeEvaluatorAgentExecutor();

        NodeExecutionTask task = createTask(Map.of(
                "orchestrationType", "SEQUENTIAL",
                "coordinationStrategy", "CENTRALIZED",
                "agentTasks", List.of(
                        Map.of("agentType", "planner-agent", "context", Map.of("step", "plan-v1")),
                        Map.of("agentType", "coder-agent", "context", Map.of("step", "execute")),
                        Map.of("agentType", "evaluator-agent", "context", Map.of("step", "evaluate")),
                        Map.of("agentType", "planner-agent", "context", Map.of("step", "plan-v2")))));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.COMPLETED, result.status());
        assertEquals("SEQUENTIAL", result.output().get("orchestrationType"));
        assertEquals(4, result.output().get("tasksExecuted"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.output().get("results");
        assertEquals("planner", results.get(0).get("agent"));
        assertEquals("coder", results.get(1).get("agent"));
        assertEquals("evaluator", results.get(2).get("agent"));
        assertEquals("planner", results.get(3).get("agent"));
    }

    @Test
    void executeFailsWhenNoObjectiveAndNoAgentTasks() {
        TestOrchestratorAgentExecutor executor = new TestOrchestratorAgentExecutor();
        executor.setObjectMapper(lenientMapper());

        NodeExecutionTask task = createTask(Map.of("taskType", "DELEGATE"));

        NodeExecutionResult result = executor.execute(task)
                .await().atMost(Duration.ofSeconds(3));

        assertEquals(NodeExecutionStatus.FAILED, result.status());
        assertNotNull(result.error());
        assertTrue(result.error().message().contains("Neither agentTasks nor objective"));
    }

    @Test
    void canHandleMatchesExecutorType() {
        OrchestratorAgentExecutor executor = new TestOrchestratorAgentExecutor();

        NodeExecutionTask matching = createTask(Map.of("agentType", "orchestrator-agent"));
        NodeExecutionTask different = createTask(Map.of("agentType", "agent-coder"));
        NodeExecutionTask missing = createTask(Map.of("objective", "x"));

        assertTrue(executor.canHandle(matching));
        assertFalse(executor.canHandle(different));
        assertFalse(executor.canHandle(missing));
    }

    private ObjectMapper lenientMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NodeExecutionTask createTask(Map<String, Object> context) {
        WorkflowRunId runId = new WorkflowRunId(UUID.randomUUID().toString());
        NodeId nodeId = new NodeId("orchestrator-test-node");
        int attempt = 1;
        return new NodeExecutionTask(
                runId,
                nodeId,
                attempt,
                ExecutionToken.create(runId, nodeId, attempt, Duration.ofMinutes(5)),
                context,
                RetryPolicy.none());
    }

    private static final class TestOrchestratorAgentExecutor extends OrchestratorAgentExecutor {
        private void setObjectMapper(ObjectMapper mapper) {
            this.objectMapper = mapper;
        }
    }

    private static final class FakeGollekInferenceService extends GollekInferenceService {
        private final AgentInferenceResponse response;
        private AgentInferenceRequest lastRequest;

        private FakeGollekInferenceService(AgentInferenceResponse response) {
            this.response = response;
        }

        @Override
        public AgentInferenceResponse inferWithFallback(AgentInferenceRequest request, String fallbackProvider) {
            this.lastRequest = request;
            return response;
        }
    }

    private static final class FakeCommonAgentExecutor extends CommonAgentExecutor {
        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
            return Uni.createFrom().item(success(task, Map.of("agent", "common")));
        }
    }

    private static final class FakePlannerAgentExecutor extends PlannerAgentExecutor {
        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
            return Uni.createFrom().item(success(task, Map.of("agent", "planner")));
        }
    }

    private static final class FakeCoderAgentExecutor extends CoderAgentExecutor {
        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
            return Uni.createFrom().item(success(task, Map.of("agent", "coder")));
        }
    }

    private static final class FakeAnalyticAgentExecutor extends AnalyticAgentExecutor {
        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
            return Uni.createFrom().item(success(task, Map.of("agent", "analytic")));
        }
    }

    private static final class FakeEvaluatorAgentExecutor extends EvaluatorAgentExecutor {
        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
            return Uni.createFrom().item(success(task, Map.of("agent", "evaluator")));
        }
    }

    private static NodeExecutionResult success(NodeExecutionTask task, Map<String, Object> output) {
        return new DefaultNodeExecutionResult(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                NodeExecutionStatus.COMPLETED,
                output,
                null,
                task.token());
    }
}
