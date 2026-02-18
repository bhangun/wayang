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
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.delay", maxConcurrentTasks = 200, supportedNodeTypes = {
                "delay", "wait" }, version = "1.0.0")
public class DelayExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(DelayExecutor.class);

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                int delayMs = (Integer) context.getOrDefault("delayMs", 1000);

                LOG.debug("Delaying execution for {} ms", delayMs);

                // This is a simulation, in real scheduler this would probably be handled by
                // scheduling
                // For short delays, we can use
                // Uni.createFrom().item().onItem().delayIt().by(...)

                return Uni.createFrom().item(task)
                                .onItem().delayIt().by(Duration.ofMillis(delayMs))
                                .map(t -> SimpleNodeExecutionResult.success(
                                                t.runId(),
                                                t.nodeId(),
                                                t.attempt(),
                                                Map.of("delayedMs", delayMs),
                                                t.token(), Duration.ZERO));
        }
}
