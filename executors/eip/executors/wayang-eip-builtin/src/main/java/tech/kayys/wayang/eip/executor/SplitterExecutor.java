package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.wayang.eip.config.SplitterConfig;
import tech.kayys.wayang.eip.strategy.SplitStrategyFactory;

import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.splitter", maxConcurrentTasks = 50, supportedNodeTypes = {
                "splitter", "iterator" }, version = "1.0.0")
public class SplitterExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(SplitterExecutor.class);

        @Inject
        SplitStrategyFactory strategyFactory;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                SplitterConfig config = SplitterConfig.fromContext(context);
                Object message = context.get("message");

                return strategyFactory.getStrategy(config.strategy())
                                .split(message, config)
                                .map(items -> {
                                        LOG.info("Split message into {} items", items.size());
                                        return SimpleNodeExecutionResult.success(
                                                        task.runId(),
                                                        task.nodeId(),
                                                        task.attempt(),
                                                        Map.of(
                                                                        "items", items,
                                                                        "parts", items,
                                                                        "count", items.size(),
                                                                        "partCount", items.size(),
                                                                        "splitAt", Instant.now().toString()),
                                                        task.token(), java.time.Duration.ZERO);
                                });
        }
}
