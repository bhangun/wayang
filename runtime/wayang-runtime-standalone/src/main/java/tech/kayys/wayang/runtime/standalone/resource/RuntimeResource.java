package tech.kayys.wayang.runtime.standalone.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.kayys.wayang.runtime.standalone.status.RuntimeStatusService;
import tech.kayys.wayang.runtime.standalone.status.RuntimeStatusSnapshot;

/**
 * Runtime integration status for standalone mode.
 */
@Path("/api/runtime")
@Produces(MediaType.APPLICATION_JSON)
public class RuntimeResource {

    @Inject RuntimeStatusService runtimeStatusService;

    @GET
    @Path("/status")
    public Uni<RuntimeStatusSnapshot> status() {
        return runtimeStatusService.collectStatus();
    }
}
