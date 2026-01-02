package tech.kayys.silat.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.silat.execution.NodeExecutionTask;
import tech.kayys.silat.model.ExecutorInfo;

@ApplicationScoped
public class RestTaskDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(RestTaskDispatcher.class);

    public Uni<Void> dispatch(NodeExecutionTask task, ExecutorInfo executor) {
        LOG.info("Dispatching task via REST to: {}", executor.endpoint());

        // TODO: Implement REST client call

        return Uni.createFrom().voidItem();
    }
}
