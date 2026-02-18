package tech.kayys.wayang.eip.executor;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.AbstractWorkflowExecutor;
import tech.kayys.gamelan.sdk.executor.core.Executor;
import tech.kayys.gamelan.sdk.executor.core.SimpleNodeExecutionResult;
import tech.kayys.wayang.eip.config.RouterConfig;
import tech.kayys.wayang.eip.service.RouteEvaluator;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.router", maxConcurrentTasks = 100, supportedNodeTypes = {
                "router", "dynamic-router" }, version = "1.0.0")
public class RouterExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(RouterExecutor.class);

        @Inject
        RouteEvaluator routeEvaluator;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                RouterConfig config = RouterConfig.fromContext(context);

                LOG.debug("Routing message with strategy: {}", config.strategy());

                return routeEvaluator.evaluateRoutes(config, context)
                                .map(selectedRoutes -> {
                                        LOG.info("Selected routes: {}", selectedRoutes);
                                        return SimpleNodeExecutionResult.success(
                                                        task.runId(),
                                                        task.nodeId(),
                                                        task.attempt(),
                                                        Map.of(
                                                                        "selectedRoutes", selectedRoutes,
                                                                        "route",
                                                                        selectedRoutes.isEmpty() ? ""
                                                                                        : selectedRoutes.get(0),
                                                                        "routedAt", Instant.now().toString()),
                                                        task.token(), java.time.Duration.ZERO);
                                });
        }
}