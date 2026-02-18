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

import tech.kayys.wayang.eip.config.TransformerConfig;
import tech.kayys.wayang.eip.service.TransformerRegistry;

import java.time.Instant;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Executor(executorType = "eip.transformer", maxConcurrentTasks = 100, supportedNodeTypes = {
                "transformer", "converter", "mapper" }, version = "1.0.0")
public class TransformerExecutor extends AbstractWorkflowExecutor {

        private static final Logger LOG = LoggerFactory.getLogger(TransformerExecutor.class);

        @Inject
        TransformerRegistry transformerRegistry;

        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
                Map<String, Object> context = task.context();
                TransformerConfig config = TransformerConfig.fromContext(context);
                Object message = context.get("message");

                LOG.debug("Transforming message with type: {}", config.transformType());

                return transformerRegistry.getTransformer(config.transformType())
                                .transform(message, config.parameters())
                                .map(transformed -> {
                                        return SimpleNodeExecutionResult.success(
                                                        task.runId(),
                                                        task.nodeId(),
                                                        task.attempt(),
                                                        Map.of(
                                                                        "message", transformed,
                                                                        "originalType",
                                                                        message != null ? message.getClass().getName()
                                                                                        : "null",
                                                                        "transformedType",
                                                                        transformed != null
                                                                                        ? transformed.getClass()
                                                                                                        .getName()
                                                                                        : "null",
                                                                        "transformedAt", Instant.now().toString()),
                                                        task.token(), java.time.Duration.ZERO);
                                });
        }
}
