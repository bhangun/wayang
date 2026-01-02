package tech.kayys.silat.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.silat.execution.NodeExecutionTask;
import tech.kayys.silat.model.ExecutorInfo;

@ApplicationScoped
public class GrpcTaskDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTaskDispatcher.class);

    public Uni<Void> dispatch(NodeExecutionTask task, ExecutorInfo executor) {
        // gRPC dispatch logic
        LOG.info("Dispatching task via gRPC to: {}", executor.endpoint());

        // TODO: Implement gRPC client call
        // This will be implemented in the gRPC module

        return Uni.createFrom().voidItem();
    }
}
