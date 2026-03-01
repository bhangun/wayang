package tech.kayys.wayang.runtime.standalone.resource;

import io.quarkus.runtime.Quarkus;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
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

    @POST
    @Path("/shutdown")
    public Response shutdown() {
        // Trigger shutdown after returning HTTP response so callers get a clean ACK.
        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            Quarkus.asyncExit(0);
        });
        shutdownThread.setName("wayang-runtime-shutdown");
        shutdownThread.setDaemon(true);
        shutdownThread.start();

        return Response.accepted()
                .entity(java.util.Map.of(
                        "accepted", true,
                        "message", "Wayang runtime shutdown requested"))
                .build();
    }
}
