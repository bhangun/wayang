package tech.kayys.wayang.runtime.standalone.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.List;
import tech.kayys.gamelan.engine.executor.ExecutorInfo;
import tech.kayys.gamelan.registry.ExecutorRegistry;

@Path("/api/v1/executors")
@Produces(MediaType.APPLICATION_JSON)
public class ExecutorsResource {

    @Inject
    ExecutorRegistry executorRegistry;

    @GET
    public Uni<List<ExecutorView>> listExecutors(@QueryParam("healthy") Boolean healthy) {
        Uni<List<ExecutorInfo>> source = Boolean.TRUE.equals(healthy)
                ? executorRegistry.getHealthyExecutors()
                : executorRegistry.getAllExecutors();

        return source.map(executors -> executors.stream()
                .map(ExecutorView::from)
                .toList());
    }

    record ExecutorView(
            String executorId,
            String executorType,
            String communicationType,
            String endpoint,
            long timeoutMillis) {

        static ExecutorView from(ExecutorInfo info) {
            Duration timeout = info.timeout();
            return new ExecutorView(
                    info.executorId(),
                    info.executorType(),
                    info.communicationType() != null ? info.communicationType().name() : null,
                    info.endpoint(),
                    timeout != null ? timeout.toMillis() : 0L);
        }
    }
}
