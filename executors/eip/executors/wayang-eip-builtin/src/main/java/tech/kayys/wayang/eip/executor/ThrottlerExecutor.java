package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.eip.config.ThrottlerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.throttler", maxConcurrentTasks = 100, supportedNodeTypes = {
                "throttler", "rate-limiter" }, version = "1.0.0")
public class ThrottlerExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(ThrottlerExecutor.class);

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                ThrottlerConfig config = ThrottlerConfig.fromContext(context);

                LOG.debug("Throttling at {} rps", config.rate());

                // Rate limiting logic would go here (using Redis or token bucket)

                return Uni.createFrom().item(SimpleNodeExecutionResult.success(
                                task.runId(),
                                task.nodeId(),
                                task.attempt(),
                                Map.of(
                                                "throttled", false,
                                                "nodeType", config.nodeType()),
                                task.token(), java.time.Duration.ZERO));
        }
}
