package tech.kayys.wayang.workflow.api.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import tech.kayys.wayang.workflow.engine.WorkflowRunManager;
import tech.kayys.wayang.workflow.service.RunCheckpointService;
import tech.kayys.wayang.workflow.v1.*;
import tech.kayys.wayang.schema.execution.ErrorPayload;
import com.google.protobuf.Empty;
import io.grpc.Status;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
@Authenticated
public class WorkflowRunGrpcService implements WorkflowRunService {

    @Inject
    WorkflowRunManager runManager;

    @Inject
    RunCheckpointService checkpointService;

    @Inject
    SecurityIdentity securityIdentity;

    private String getTenantId() {
        if (securityIdentity.isAnonymous()) {
            // Should be caught by @Authenticated, but as a safeguard
            throw new IllegalStateException("Anonymous access not allowed");
        }

        // Try to get tenant_id from attributes (mapped from claims)
        // If using OIDC, claims are often available as attributes or via Principal if
        // cast to JsonWebToken
        // For simplicity/generality, we assume the Principal Name is the tenant or user
        // effectively acting as a tenant in this context.
        // In a real multi-tenant setup, you'd extract "tenant_id" claim.
        // String tenantId = securityIdentity.getAttribute("tenant_id");
        // if (tenantId != null) return tenantId;

        return securityIdentity.getPrincipal().getName();
    }

    private <T> Uni<T> handleErrors(Throwable throwable) {
        if (throwable instanceof tech.kayys.wayang.workflow.exception.RunNotFoundException) {
            return Uni.createFrom()
                    .failure(Status.NOT_FOUND.withDescription(throwable.getMessage()).asRuntimeException());
        }
        if (throwable instanceof IllegalArgumentException) {
            return Uni.createFrom()
                    .failure(Status.INVALID_ARGUMENT.withDescription(throwable.getMessage()).asRuntimeException());
        }
        if (throwable instanceof IllegalStateException) {
            return Uni.createFrom()
                    .failure(Status.FAILED_PRECONDITION.withDescription(throwable.getMessage()).asRuntimeException());
        }
        return Uni.createFrom().failure(Status.INTERNAL.withDescription(throwable.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<WorkflowRun> createRun(tech.kayys.wayang.workflow.v1.CreateRunRequest request) {
        Map<String, Object> inputs = request.getInputsMap() != null ? new java.util.HashMap<>(request.getInputsMap())
                : Collections.emptyMap();
        tech.kayys.wayang.workflow.api.dto.CreateRunRequest domainRequest = new tech.kayys.wayang.workflow.api.dto.CreateRunRequest(
                request.getWorkflowId(),
                request.getWorkflowVersion().isEmpty() ? null : request.getWorkflowVersion(),
                inputs,
                "grpc");

        return runManager.createRun(domainRequest, getTenantId())
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<WorkflowRun> getRun(GetRunRequest request) {
        return runManager.getRun(request.getRunId())
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<ListRunsResponse> listRuns(ListRunsRequest request) {
        final String tenantId = getTenantId();

        tech.kayys.wayang.workflow.api.model.RunStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = tech.kayys.wayang.workflow.api.model.RunStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status for filtering
            }
        }

        int page = request.getPagination().getPage();
        int size = request.getPagination().getSize() > 0 ? request.getPagination().getSize() : 20;

        return runManager.queryRuns(tenantId, request.getWorkflowId(), status, page, size)
                .map(result -> ListRunsResponse.newBuilder()
                        .addAllRuns(result.runs().stream().map(this::toProto).collect(Collectors.toList()))
                        .setPagination(PaginationResponse.newBuilder()
                                .setPage(page)
                                .setSize(size)
                                .setTotalElements(result.totalElements())
                                .setTotalPages(result.totalPages())
                                .build())
                        .build())
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<WorkflowRun> startRun(RunIdRequest request) {
        return runManager.startRun(request.getRunId(), getTenantId())
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<WorkflowRun> suspendRun(SuspendRunRequest request) {
        return runManager.suspendRun(request.getRunId(), getTenantId(), request.getReason(), request.getHumanTaskId())
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<WorkflowRun> resumeRun(ResumeRunRequest request) {
        Map<String, Object> resumeData = request.getResumeDataMap() != null
                ? new java.util.HashMap<>(request.getResumeDataMap())
                : null;
        return runManager
                .resumeRun(request.getRunId(), getTenantId(), request.getHumanTaskId(), resumeData)
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<Empty> cancelRun(CancelRunRequest request) {
        return runManager.cancelRun(request.getRunId(), getTenantId(), request.getReason())
                .map(v -> Empty.getDefaultInstance())
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<WorkflowRun> completeRun(CompleteRunRequest request) {
        Map<String, Object> outputs = request.getOutputsMap() != null ? new java.util.HashMap<>(request.getOutputsMap())
                : Collections.emptyMap();
        return runManager.completeRun(request.getRunId(), getTenantId(), outputs)
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<WorkflowRun> failRun(FailRunRequest request) {
        ErrorPayload error = new ErrorPayload(
                null,
                "FAIL_RUN_API",
                Collections.emptyMap(),
                false,
                "workflow-engine",
                request.getError(),
                500,
                null,
                java.time.LocalDateTime.now(),
                null,
                null,
                null,
                null);
        return runManager
                .failRun(request.getRunId(), getTenantId(), error)
                .map(this::toProto)
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<ListCheckpointsResponse> listCheckpoints(RunIdRequest request) {
        // Assuming listCheckpoints logic also needs tenant check, but CheckpointService
        // might not enforce it yet.
        // Ideally we should check if the run belongs to the tenant first.
        return runManager.getRun(request.getRunId()) // Check existence and tenant
                .chain(run -> {
                    if (!run.getTenantId().equals(getTenantId())) {
                        return Uni.createFrom().failure(new IllegalStateException("Tenant mismatch"));
                    }
                    return checkpointService.listCheckpoints(request.getRunId());
                })
                .map(list -> ListCheckpointsResponse.newBuilder()
                        .addAllCheckpoints(list.stream().map(this::toProtoCheckpoint).collect(Collectors.toList()))
                        .build())
                .onFailure().recoverWithUni(this::handleErrors);
    }

    @Override
    public Uni<ActiveRunCount> getActiveCount(Empty request) {
        return runManager.getActiveRunsCount(getTenantId())
                .map(count -> ActiveRunCount.newBuilder().setCount(count).build())
                .onFailure().recoverWithUni(this::handleErrors);
    }

    // Mappers

    private WorkflowRun toProto(tech.kayys.wayang.workflow.domain.WorkflowRun domain) {
        if (domain == null)
            return null;
        WorkflowRun.Builder builder = WorkflowRun.newBuilder()
                .setRunId(domain.getRunId())
                .setWorkflowId(domain.getWorkflowId())
                .setWorkflowVersion(domain.getWorkflowVersion() != null ? domain.getWorkflowVersion() : "")
                .setStatus(domain.getStatus().name())
                .setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toEpochMilli() : 0)
                .setAttemptNumber(domain.getAttemptNumber())
                .setMaxAttempts(domain.getMaxAttempts());

        if (domain.getPhase() != null)
            builder.setPhase(domain.getPhase().name());
        if (domain.getStartedAt() != null)
            builder.setStartedAt(domain.getStartedAt().toEpochMilli());
        if (domain.getCompletedAt() != null)
            builder.setCompletedAt(domain.getCompletedAt().toEpochMilli());
        if (domain.getErrorMessage() != null)
            builder.setErrorMessage(domain.getErrorMessage());

        if (domain.getOutputs() != null) {
            domain.getOutputs().forEach((k, v) -> {
                if (k != null && v != null)
                    builder.putOutputs(k, v.toString());
            });
        }

        // Populate nodes_executed map if needed.
        // Currently relying on Checkpoints for node history or separate call.

        return builder.build();
    }

    private Checkpoint toProtoCheckpoint(tech.kayys.wayang.workflow.domain.Checkpoint domain) {
        return Checkpoint.newBuilder()
                .setCheckpointId(domain.getCheckpointId())
                .setRunId(domain.getRunId())
                .setSequenceNumber(domain.getSequenceNumber())
                .setStatus(domain.getStatus())
                .setNodesExecuted(domain.getNodesExecuted())
                .setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toEpochMilli() : 0)
                .build();
    }
}
