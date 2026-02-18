package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.multicast", maxConcurrentTasks = 100, supportedNodeTypes = {
                "multicast", "recipient-list" }, version = "1.0.0")
public class MulticastExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(MulticastExecutor.class);

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();

                @SuppressWarnings("unchecked")
                List<String> recipients = (List<String>) context.getOrDefault("recipients", List.of());
                boolean parallel = (Boolean) context.getOrDefault("parallel", false);

                LOG.info("Multicasting to {} recipients (parallel: {})", recipients.size(), parallel);

                return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                                task.runId(),
                                task.nodeId(),
                                task.attempt(),
                                Map.of(
                                                "recipients", recipients,
                                                "strategy", parallel ? "parallel" : "sequential",
                                                "multicastedAt", Instant.now().toString()),
                                task.token(), Duration.ZERO));
        }
}
