package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import tech.kayys.wayang.plugin.ControlPlaneExecutorRegistry;
import tech.kayys.wayang.plugin.CommunicationProtocol;
import tech.kayys.wayang.plugin.executor.ExecutorRegistration;
import tech.kayys.wayang.plugin.executor.ExecutorStatus;
import tech.kayys.wayang.plugin.executor.ExecutorMetadata;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Executor registry and management API.
 */
@Path("/api/v1/control-plane/executors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Executors", description = "Executor registry and management")
public class ExecutorRegistryResource {

    private static final Logger LOG = Logger.getLogger(ExecutorRegistryResource.class);

    @Inject
    ControlPlaneExecutorRegistry executorRegistry;

    /**
     * Executor self-registration endpoint.
     * Called by executors on startup.
     */
    @POST
    @Path("/register")
    @Operation(summary = "Register executor", description = "Executors call this on startup to register themselves")
    public Uni<RestResponse<ExecutorRegistrationResponse>> registerExecutor(
            @Valid ExecutorRegistrationRequest request) {

        LOG.infof("Registering executor: %s (%s) at %s",
                request.executorId(),
                request.protocol(),
                request.endpoint());

        ExecutorRegistration registration = new ExecutorRegistration();
        registration.executorId = request.executorId();
        registration.executorType = request.executorType();
        registration.endpoint = URI.create(request.endpoint());
        registration.protocol = request.protocol();
        registration.capabilities = new HashSet<>(request.capabilities());
        registration.supportedNodes = new HashSet<>(request.supportedNodes());
        registration.metadata = request.metadata();
        registration.registeredAt = Instant.now();

        return executorRegistry.register(registration)
                .map(v -> RestResponse.ok(new ExecutorRegistrationResponse(
                        true,
                        registration.executorId,
                        "Executor registered successfully")))
                .onFailure().recoverWithItem(error -> RestResponse.status(RestResponse.Status.INTERNAL_SERVER_ERROR,
                        new ExecutorRegistrationResponse(
                                false,
                                request.executorId(),
                                "Registration failed: " + error.getMessage())));
    }

    /**
     * Executor heartbeat endpoint.
     */
    @POST
    @Path("/{executorId}/heartbeat")
    @Operation(summary = "Executor heartbeat", description = "Executors send periodic heartbeats to indicate health")
    public Uni<RestResponse<Void>> heartbeat(
            @PathParam("executorId") String executorId) {

        return Uni.createFrom().item(() -> {
            ExecutorRegistration registration = executorRegistry.get(executorId);
            if (registration == null) {
                return RestResponse.notFound();
            }

            registration.lastHeartbeat = Instant.now();
            registration.status = ExecutorStatus.HEALTHY;

            LOG.debugf("Heartbeat received from executor: %s", executorId);

            return RestResponse.ok();
        });
    }

    /**
     * List all executors.
     */
    @GET
    @Operation(summary = "List all registered executors")
    public Uni<List<ExecutorSummary>> listExecutors(
            @QueryParam("status") ExecutorStatus status,
            @QueryParam("capability") String capability) {

        return Uni.createFrom().item(() -> {
            List<ExecutorRegistration> executors;

            if (capability != null) {
                executors = executorRegistry.getByCapability(capability);
            } else {
                executors = executorRegistry.getAll();
            }

            return executors.stream()
                    .filter(e -> status == null || e.status == status)
                    .map(e -> new ExecutorSummary(
                            e.executorId,
                            e.executorType,
                            e.protocol,
                            e.status,
                            e.endpoint != null ? e.endpoint.toString() : null,
                            new ArrayList<>(e.capabilities),
                            new ArrayList<>(e.supportedNodes),
                            e.registeredAt,
                            e.lastHeartbeat))
                    .toList();
        });
    }

    /**
     * Get executor details.
     */
    @GET
    @Path("/{executorId}")
    @Operation(summary = "Get executor details")
    public Uni<RestResponse<ExecutorDetails>> getExecutor(
            @PathParam("executorId") String executorId) {

        return Uni.createFrom().item(() -> {
            ExecutorRegistration executor = executorRegistry.get(executorId);
            if (executor == null) {
                return RestResponse.notFound();
            }

            return RestResponse.ok(new ExecutorDetails(
                    executor.executorId,
                    executor.executorType,
                    executor.protocol,
                    executor.status,
                    executor.endpoint != null ? executor.endpoint.toString() : null,
                    new ArrayList<>(executor.capabilities),
                    new ArrayList<>(executor.supportedNodes),
                    executor.metadata,
                    executor.registeredAt,
                    executor.lastHeartbeat));
        });
    }

    /**
     * Unregister executor.
     */
    @DELETE
    @Path("/{executorId}")
    @Operation(summary = "Unregister executor")
    public Uni<RestResponse<Void>> unregisterExecutor(
            @PathParam("executorId") String executorId) {

        return Uni.createFrom().item(() -> {
            executorRegistry.unregister(executorId);
            LOG.infof("Executor unregistered: %s", executorId);
            return RestResponse.ok();
        });
    }
}

/**
 * Executor registration request
 */
record ExecutorRegistrationRequest(
        String executorId,
        String executorType,
        String endpoint,
        CommunicationProtocol protocol,
        List<String> capabilities,
        List<String> supportedNodes,
        ExecutorMetadata metadata) {
}

/**
 * Executor registration response
 */
record ExecutorRegistrationResponse(
        boolean success,
        String executorId,
        String message) {
}

/**
 * Executor summary for listing
 */
record ExecutorSummary(
        String executorId,
        String executorType,
        CommunicationProtocol protocol,
        ExecutorStatus status,
        String endpoint,
        List<String> capabilities,
        List<String> supportedNodes,
        Instant registeredAt,
        Instant lastHeartbeat) {
}

/**
 * Detailed executor information
 */
record ExecutorDetails(
        String executorId,
        String executorType,
        CommunicationProtocol protocol,
        ExecutorStatus status,
        String endpoint,
        List<String> capabilities,
        List<String> supportedNodes,
        ExecutorMetadata metadata,
        Instant registeredAt,
        Instant lastHeartbeat) {
}
