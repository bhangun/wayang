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
import tech.kayys.wayang.plugin.execution.ExecutionContract;
import tech.kayys.wayang.plugin.execution.ExecutionContext;
import tech.kayys.wayang.plugin.execution.ExecutionContractBuilder;

import java.util.Map;

/**
 * Execution contract management API.
 */
@Path("/api/v1/control-plane/execution")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Execution", description = "Execution contract management")
public class ExecutionContractResource {

    private static final Logger LOG = Logger.getLogger(ExecutionContractResource.class);

    @Inject
    ExecutionContractBuilder contractBuilder;

    /**
     * Create execution contract.
     * Called by Workflow Engine when it needs to execute a node.
     */
    @POST
    @Path("/create-contract")
    @Operation(summary = "Create execution contract", description = "Workflow Engine calls this to get execution contract for a node")
    public Uni<RestResponse<ExecutionContract>> createContract(
            @Valid ContractCreationRequest request) {

        LOG.infof("Creating execution contract for node %s in workflow %s",
                request.nodeType(), request.workflowRunId());

        return contractBuilder.build(
                request.workflowRunId(),
                request.nodeType(),
                request.nodeInstanceId(),
                request.inputs(),
                request.config(),
                request.context())
                .map(RestResponse::ok)
                .onFailure().recoverWithItem(error -> {
                    LOG.errorf(error, "Failed to create execution contract");
                    return RestResponse.status(
                            RestResponse.Status.INTERNAL_SERVER_ERROR);
                });
    }
}

/**
 * Contract creation request
 */
record ContractCreationRequest(
        String workflowRunId,
        String nodeType,
        String nodeInstanceId,
        Map<String, Object> inputs,
        Map<String, Object> config,
        ExecutionContext context) {
}
