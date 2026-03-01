package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tech.kayys.wayang.plugin.registry.executor.ExecutorStatus;

/**
 * Backward-compatible executor listing endpoint for UI clients.
 */
@Path("/api/v1/registry/executors")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Executors", description = "Executor registry and management")
public class ExecutorRegistryAliasResource {

    @Inject
    ExecutorRegistryResource delegate;

    @GET
    @Operation(summary = "List all registered executors")
    public Uni<List<ExecutorSummary>> listExecutors(
            @QueryParam("status") ExecutorStatus status,
            @QueryParam("capability") String capability) {
        return delegate.listExecutors(status, capability);
    }
}
