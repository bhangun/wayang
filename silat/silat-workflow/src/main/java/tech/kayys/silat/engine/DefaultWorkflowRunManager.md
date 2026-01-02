package tech.kayys.silat.engine;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kayys.silat.distributed.DistributedLockManager;
import tech.kayys.silat.execution.ExecutionHistory;
import tech.kayys.silat.model.ExternalSignal;
import tech.kayys.silat.execution.NodeExecutionResult;
import tech.kayys.silat.execution.NodeExecutionTask;
import tech.kayys.silat.model.CallbackConfig;
import tech.kayys.silat.model.CallbackRegistration;
import tech.kayys.silat.model.ConcurrencyException;
import tech.kayys.silat.model.CreateRunRequest;
import tech.kayys.silat.model.ErrorInfo;
import tech.kayys.silat.model.EventStore;
import tech.kayys.silat.model.ExecutionToken;
import tech.kayys.silat.model.NodeExecution;
import tech.kayys.silat.model.NodeId;
import tech.kayys.silat.model.RunStatus;
import tech.kayys.silat.model.Signal;
import tech.kayys.silat.model.TenantId;
import tech.kayys.silat.model.ValidationResult;
import tech.kayys.silat.model.WorkflowDefinitionId;
import tech.kayys.silat.model.WorkflowRun;
import tech.kayys.silat.model.WorkflowRunId;
import tech.kayys.silat.model.WorkflowRunSnapshot;
import tech.kayys.silat.model.event.ExecutionEvent;
import tech.kayys.silat.repository.WorkflowRunRepository;
import tech.kayys.silat.saga.CompensationCoordinator;
import tech.kayys.silat.scheduler.WorkflowScheduler;
import tech.kayys.silat.security.TenantSecurityContext;
import tech.kayys.silat.workflow.WorkflowDefinitionRegistry;
import tech.kayys.silat.workflow.WorkflowExecutionEngine;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * ============================================================================
 * WORKFLOW RUN MANAGER - The Orchestrator
 * ============================================================================
 * 
 * Primary responsibilities:
 * 1. Lifecycle management of workflow runs
 * 2. State transitions with validation
 * 3. Event sourcing and persistence
 * 4. Distributed locking for concurrency control
 * 5. Integration with executors via scheduler
 * 6. Tenant isolation enforcement
 * 
 * Architecture Pattern: CQRS + Event Sourcing
 * - Commands: Modify state and produce events
 * - Queries: Read from materialized views
 * - Events: Source of truth stored in event store
 * 
 * Concurrency: Optimistic locking with distributed locks for critical sections
 */
@ApplicationScoped
public class DefaultWorkflowRunManager implements WorkflowRunManager {

        private static final Logger LOG = LoggerFactory.getLogger(DefaultWorkflowRunManager.class);
        private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(30);
        private static final Duration EXECUTION_TOKEN_VALIDITY = Duration.ofHours(1);

        @Inject
        WorkflowRunRepository repository;

        @Inject
        EventStore eventStore;

        @Inject
        WorkflowScheduler scheduler;

        @Inject
        DistributedLockManager lockManager;

        @Inject
        WorkflowDefinitionRegistry definitionRegistry;

        @Inject
        WorkflowExecutionEngine executionEngine;

        @Inject
        CompensationCoordinator compensationCoordinator;

        @Inject
        TenantSecurityContext tenantContext;

        // ==================== LIFECYCLE OPERATIONS ====================

        @Override
        public Uni<WorkflowRun> createRun(CreateRunRequest request, TenantId tenantId) {
                LOG.info("Creating workflow run for definition: {} in tenant: {}",
                                request.getWorkflowId(), tenantId.value());

                // Validate inputs
                Uni<CreateRunRequest> validatedRequest = validateCreateRunRequest(request);
                if (validatedRequest == null) {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("CreateRunRequest cannot be null"));
                }

                return validatedRequest
                                .flatMap(validRequest -> validateTenantAccess(tenantId)
                                                .onFailure().recoverWithUni(failure -> {
                                                        LOG.error("Tenant access validation failed for tenant: {}",
                                                                        tenantId.value(), failure);
                                                        return Uni.createFrom().failure(failure);
                                                })
                                                .flatMap(v -> definitionRegistry.getDefinition(
                                                                WorkflowDefinitionId.of(
                                                                                validRequest.getWorkflowId()),
                                                                tenantId))
                                                .onItem().ifNull()
                                                .failWith(() -> new NoSuchElementException(
                                                                "Workflow definition not found: "
                                                                                + validRequest.getWorkflowId()))
                                                .flatMap(definition -> {
                                                        // Validate workflow definition
                                                        if (!definition.isValid()) {
                                                                return Uni.createFrom().failure(
                                                                                new IllegalArgumentException(
                                                                                                "Invalid workflow definition: "
                                                                                                                + validRequest.getWorkflowId()));
                                                        }

                                                        // Create aggregate
                                                        WorkflowRun run = WorkflowRun.create(
                                                                        tenantId,
                                                                        definition,
                                                                        validRequest.getInputs() != null
                                                                                        ? validRequest.getInputs()
                                                                                        : Map.of());

                                                        // Capture events before persistence clears them
                                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                                        // Persist aggregate and events
                                                        return persistRun(run)
                                                                        .flatMap(persisted -> publishEvents(persisted,
                                                                                        events))
                                                                        .onFailure()
                                                                        .invoke(throwable -> LOG.error(
                                                                                        "Failed to persist or publish events for run: {}",
                                                                                        run.getId().value(), throwable))
                                                                        .replaceWith(run);
                                                }))
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to create workflow run for definition: {}",
                                                request.getWorkflowId(), throwable));
        }

        /**
         * Validate CreateRunRequest inputs
         */
        private Uni<CreateRunRequest> validateCreateRunRequest(CreateRunRequest request) {
                if (request == null) {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("CreateRunRequest cannot be null"));
                }
                if (request.getWorkflowId() == null || request.getWorkflowId().trim().isEmpty()) {
                        return Uni.createFrom().failure(
                                        new IllegalArgumentException("Workflow definition ID cannot be null or empty"));
                }
                return Uni.createFrom().item(request);
        }

        @Override
        public Uni<WorkflowRun> startRun(WorkflowRunId runId, TenantId tenantId) {
                LOG.info("Starting workflow run: {} in tenant: {}", runId.value(), tenantId.value());

                if (runId == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("WorkflowRunId cannot be null"));
                }
                if (tenantId == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("TenantId cannot be null"));
                }

                return withDistributedLock(runId, () -> loadRun(runId, tenantId)
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to load run for start operation: {}",
                                                runId.value(), throwable))
                                .flatMap(run -> {
                                        // Start the workflow
                                        run.start();

                                        // Capture events
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        // Persist state
                                        return persistRun(run)
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to persist run after start: {}",
                                                                        runId.value(), throwable))
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to publish events after start: {}",
                                                                        runId.value(), throwable))
                                                        .flatMap(persisted -> scheduleReadyNodes(persisted))
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to schedule nodes after start: {}",
                                                                        runId.value(), throwable))
                                                        .replaceWith(run);
                                }));
        }

        @Override
        public Uni<WorkflowRun> suspendRun(
                        WorkflowRunId runId,
                        TenantId tenantId,
                        String reason,
                        NodeId waitingOnNodeId) {

                LOG.info("Suspending workflow run: {} - reason: {}", runId.value(), reason);

                return withDistributedLock(runId, () -> loadRun(runId, tenantId)
                                .flatMap(run -> {
                                        run.suspend(reason, waitingOnNodeId);
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        return persistRun(run)
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .replaceWith(run);
                                }));
        }

        @Override
        public Uni<WorkflowRun> resumeRun(
                        WorkflowRunId runId,
                        TenantId tenantId,
                        Map<String, Object> resumeData) {

                LOG.info("Resuming workflow run: {}", runId.value());

                return withDistributedLock(runId, () -> loadRun(runId, tenantId)
                                .flatMap(run -> {
                                        run.resume(resumeData);
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        return persistRun(run)
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .flatMap(persisted -> scheduleReadyNodes(persisted))
                                                        .replaceWith(run);
                                }));
        }

        @Override
        public Uni<Void> cancelRun(WorkflowRunId runId, TenantId tenantId, String reason) {
                LOG.info("Cancelling workflow run: {} - reason: {}", runId.value(), reason);

                return withDistributedLock(runId, () -> loadRun(runId, tenantId)
                                .flatMap(run -> {
                                        run.cancel(reason);
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        return persistRun(run)
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .flatMap(persisted -> cancelScheduledNodes(persisted))
                                                        .flatMap(v -> initiateCompensationIfNeeded(run))
                                                        .replaceWithVoid();
                                }));
        }

        @Override
        public Uni<WorkflowRun> completeRun(
                        WorkflowRunId runId,
                        TenantId tenantId,
                        Map<String, Object> outputs) {

                LOG.info("Completing workflow run: {}", runId.value());

                return withDistributedLock(runId, () -> loadRun(runId, tenantId)
                                .flatMap(run -> {
                                        run.complete(outputs);
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        return persistRun(run)
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .replaceWith(run);
                                }));
        }

        @Override
        public Uni<WorkflowRun> failRun(WorkflowRunId runId, TenantId tenantId, ErrorInfo error) {
                LOG.error("Failing workflow run: {} - error: {}", runId.value(), error.message());

                return withDistributedLock(runId, () -> loadRun(runId, tenantId)
                                .flatMap(run -> {
                                        run.fail(error);
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        return persistRun(run)
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .flatMap(persisted -> initiateCompensationIfNeeded(persisted))
                                                        .replaceWith(run);
                                }));
        }

        // ==================== NODE EXECUTION FEEDBACK ====================

        @Override
        public Uni<Void> handleNodeResult(WorkflowRunId runId, NodeExecutionResult result) {
                LOG.debug("Handling node result for run: {}, node: {}, status: {}",
                                runId.value(), result.nodeId().value(), result.status());

                if (runId == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("WorkflowRunId cannot be null"));
                }
                if (result == null) {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("NodeExecutionResult cannot be null"));
                }
                if (result.nodeId() == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("NodeId cannot be null"));
                }
                if (result.executionToken() == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Execution token cannot be null"));
                }

                return withDistributedLock(runId, () -> loadRunForUpdate(runId)
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to load run for node result handling: {}",
                                                runId.value(), throwable))
                                .flatMap(run -> {
                                        // Validate execution token
                                        return validateExecutionToken(result.executionToken())
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Token validation failed for run: {}, node: {}",
                                                                        runId.value(), result.nodeId().value(),
                                                                        throwable))
                                                        .flatMap(valid -> {
                                                                if (!valid) {
                                                                        LOG.warn("Invalid execution token for run: {}, node: {}",
                                                                                        runId.value(),
                                                                                        result.nodeId().value());
                                                                        return Uni.createFrom().failure(
                                                                                        new SecurityException(
                                                                                                        "Invalid execution token"));
                                                                }

                                                                // Apply result to aggregate
                                                                switch (result.status()) {
                                                                        case COMPLETED -> run.completeNode(
                                                                                        result.nodeId(),
                                                                                        result.attempt(),
                                                                                        result.output());

                                                                        case FAILED -> run.failNode(
                                                                                        result.nodeId(),
                                                                                        result.attempt(),
                                                                                        result.error());

                                                                        default -> {
                                                                                LOG.warn("Unexpected status for node result: {}",
                                                                                                result.status());
                                                                                return Uni.createFrom().failure(
                                                                                                new IllegalArgumentException(
                                                                                                                "Unexpected status: "
                                                                                                                                + result
                                                                                                                                                .status()));
                                                                        }
                                                                }

                                                                List<ExecutionEvent> events = run
                                                                                .getUncommittedEvents();

                                                                return persistRun(run)
                                                                                .onFailure()
                                                                                .invoke(throwable -> LOG.error(
                                                                                                "Failed to persist run after node result: {}",
                                                                                                runId.value(),
                                                                                                throwable))
                                                                                .flatMap(persisted -> publishEvents(
                                                                                                persisted, events))
                                                                                .onFailure()
                                                                                .invoke(throwable -> LOG.error(
                                                                                                "Failed to publish events after node result: {}",
                                                                                                runId.value(),
                                                                                                throwable))
                                                                                .flatMap(persisted -> scheduleReadyNodes(
                                                                                                persisted))
                                                                                .onFailure()
                                                                                .invoke(throwable -> LOG.error(
                                                                                                "Failed to schedule nodes after node result: {}",
                                                                                                runId.value(),
                                                                                                throwable))
                                                                                .replaceWithVoid();
                                                        });
                                }));
        }

        @Override
        public Uni<Void> signal(WorkflowRunId runId, Signal signal) {
                LOG.info("Received signal for run: {}, signal: {}", runId.value(),
                                signal != null ? signal.name() : "null");

                if (runId == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("WorkflowRunId cannot be null"));
                }
                if (signal == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Signal cannot be null"));
                }
                if (signal.name() == null || signal.name().trim().isEmpty()) {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("Signal name cannot be null or empty"));
                }

                // Sanitize signal name to prevent injection attacks
                String sanitizedName = signal.name().trim();
                if (!sanitizedName.matches("^[a-zA-Z0-9_-]+$")) {
                        LOG.warn("Invalid signal name format: {}", sanitizedName);
                        return Uni.createFrom().failure(new IllegalArgumentException("Invalid signal name format"));
                }

                return withDistributedLock(runId, () -> loadRunForUpdate(runId)
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to load run for signaling: {}", runId.value(),
                                                throwable))
                                .flatMap(run -> {
                                        run.signal(signal);
                                        List<ExecutionEvent> events = run.getUncommittedEvents();

                                        return persistRun(run)
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to persist run after signaling: {}",
                                                                        runId.value(), throwable))
                                                        .flatMap(persisted -> publishEvents(persisted, events))
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to publish events after signaling: {}",
                                                                        runId.value(), throwable))
                                                        .flatMap(persisted -> scheduleReadyNodes(persisted))
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to schedule nodes after signaling: {}",
                                                                        runId.value(), throwable))
                                                        .replaceWithVoid();
                                }));
        }

        // ==================== QUERY OPERATIONS ====================

        @Override
        public Uni<WorkflowRun> getRun(WorkflowRunId runId, TenantId tenantId) {
                return validateTenantAccess(tenantId)
                                .flatMap(v -> repository.findById(runId, tenantId))
                                .onItem().ifNull()
                                .failWith(() -> new NoSuchElementException("Workflow run not found: " + runId.value()));
        }

        @Override
        public Uni<WorkflowRunSnapshot> getSnapshot(WorkflowRunId runId, TenantId tenantId) {
                return getRun(runId, tenantId)
                                .map(WorkflowRun::createSnapshot);
        }

        @Override
        public Uni<ExecutionHistory> getExecutionHistory(WorkflowRunId runId, TenantId tenantId) {
                return validateTenantAccess(tenantId)
                                .flatMap(v -> eventStore.getEvents(runId))
                                .map(events -> ExecutionHistory.fromEvents(runId, events));
        }

        @Override
        public Uni<List<WorkflowRun>> queryRuns(
                        TenantId tenantId,
                        WorkflowDefinitionId definitionId,
                        RunStatus status,
                        int page,
                        int size) {

                return validateTenantAccess(tenantId)
                                .flatMap(v -> repository.query(tenantId, definitionId, status, page, size));
        }

        @Override
        public Uni<Long> getActiveRunsCount(TenantId tenantId) {
                return validateTenantAccess(tenantId)
                                .flatMap(v -> repository.countActiveRuns(tenantId));
        }

        @Override
        public Uni<ValidationResult> validateTransition(
                        WorkflowRunId runId,
                        RunStatus targetStatus) {

                return loadRunForUpdate(runId)
                                .map(run -> {
                                        if (run.getStatus().canTransitionTo(targetStatus)) {
                                                return ValidationResult.success();
                                        } else {
                                                return ValidationResult.failure(
                                                                String.format("Invalid transition from %s to %s",
                                                                                run.getStatus(), targetStatus));
                                        }
                                });
        }

        // ==================== TOKEN MANAGEMENT ====================

        @Override
        public Uni<ExecutionToken> createExecutionToken(
                        WorkflowRunId runId,
                        NodeId nodeId,
                        int attempt) {

                ExecutionToken token = ExecutionToken.create(
                                runId,
                                nodeId,
                                attempt,
                                EXECUTION_TOKEN_VALIDITY);

                // Store token in cache for validation
                return repository.storeToken(token)
                                .replaceWith(token);
        }

        private Uni<Boolean> validateExecutionToken(ExecutionToken token) {
                if (token == null) {
                        LOG.warn("Null execution token provided for validation");
                        return Uni.createFrom().item(false);
                }

                if (token.isExpired()) {
                        LOG.warn("Expired execution token provided: {}", token.token());
                        return Uni.createFrom().item(false);
                }

                return repository.validateToken(token)
                                .onItem().ifNull()
                                .transform(result -> false)
                                .onFailure().invoke(throwable -> LOG.error("Token validation failed", throwable));
        }

        // ==================== EXTERNAL INTEGRATION ====================

        @Override
        public Uni<Void> onNodeExecutionCompleted(
                        NodeExecutionResult result,
                        String executorSignature) {

                // Validate inputs first
                Uni<NodeExecutionResult> validatedResult = validateNodeExecutionResult(result);
                if (validatedResult == null) {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("NodeExecutionResult cannot be null"));
                }

                return validatedResult
                                .flatMap(validResult -> verifyExecutorSignature(executorSignature)
                                                .onFailure()
                                                .invoke(throwable -> LOG.error("Executor signature verification failed",
                                                                throwable))
                                                .flatMap(verified -> {
                                                        if (!verified) {
                                                                LOG.warn("Invalid executor signature for run: {}",
                                                                                validResult.runId().value());
                                                                return Uni.createFrom().failure(
                                                                                new SecurityException(
                                                                                                "Invalid executor signature"));
                                                        }

                                                        return handleNodeResult(validResult.runId(), validResult);
                                                }));
        }

        /**
         * Validate NodeExecutionResult inputs
         */
        private Uni<NodeExecutionResult> validateNodeExecutionResult(NodeExecutionResult result) {
                if (result == null) {
                        return Uni.createFrom()
                                        .failure(new IllegalArgumentException("NodeExecutionResult cannot be null"));
                }
                if (result.runId() == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Run ID cannot be null"));
                }
                if (result.nodeId() == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Node ID cannot be null"));
                }
                if (result.status() == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Status cannot be null"));
                }
                return Uni.createFrom().item(result);
        }

        @Override
        public Uni<Void> onExternalSignal(
                        WorkflowRunId runId,
                        ExternalSignal externalSignal,
                        String callbackToken) {

                if (runId == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("WorkflowRunId cannot be null"));
                }
                if (externalSignal == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("ExternalSignal cannot be null"));
                }
                if (callbackToken == null || callbackToken.trim().isEmpty()) {
                        return Uni.createFrom().failure(
                                        new IllegalArgumentException("Callback token cannot be null or empty"));
                }

                // Sanitize callback token
                String sanitizedToken = callbackToken.trim();
                if (!sanitizedToken.matches("^[a-zA-Z0-9\\-_]+$")) {
                        LOG.warn("Invalid callback token format for run: {}", runId.value());
                        return Uni.createFrom().failure(new SecurityException("Invalid callback token format"));
                }

                // Verify callback token
                return verifyCallbackToken(runId, sanitizedToken)
                                .onFailure()
                                .invoke(throwable -> LOG.error("Callback token verification failed for run: {}",
                                                runId.value(), throwable))
                                .flatMap(verified -> {
                                        if (!verified) {
                                                LOG.warn("Invalid callback token for run: {}", runId.value());
                                                return Uni.createFrom().failure(
                                                                new SecurityException("Invalid callback token"));
                                        }

                                        // Validate external signal content
                                        if (externalSignal.name() == null || externalSignal.name().trim().isEmpty()) {
                                                return Uni.createFrom().failure(new IllegalArgumentException(
                                                                "External signal name cannot be null or empty"));
                                        }

                                        // Sanitize signal name
                                        String sanitizedName = externalSignal.name().trim();
                                        if (!sanitizedName.matches("^[a-zA-Z0-9_-]+$")) {
                                                LOG.warn("Invalid external signal name format: {}", sanitizedName);
                                                return Uni.createFrom().failure(new IllegalArgumentException(
                                                                "Invalid signal name format"));
                                        }

                                        Signal signal = new Signal(
                                                        sanitizedName,
                                                        externalSignal.targetNodeId(),
                                                        externalSignal.payload(),
                                                        Instant.now());

                                        return signal(runId, signal);
                                });
        }

        @Override
        public Uni<CallbackRegistration> registerCallback(
                        WorkflowRunId runId,
                        NodeId nodeId,
                        CallbackConfig config) {

                String callbackToken = UUID.randomUUID().toString();

                CallbackRegistration registration = new CallbackRegistration(
                                callbackToken,
                                runId,
                                nodeId,
                                config.callbackUrl(),
                                Instant.now().plus(config.expirationDuration()));

                return repository.storeCallback(registration)
                                .replaceWith(registration);
        }

        // ==================== PRIVATE HELPERS ====================

        /**
         * Execute operation with distributed lock
         */
        private <T> Uni<T> withDistributedLock(WorkflowRunId runId, java.util.function.Supplier<Uni<T>> operation) {
                String lockKey = "workflow:run:" + runId.value();

                return lockManager.acquireLock(lockKey, LOCK_TIMEOUT)
                                .onItem().ifNull()
                                .failWith(() -> new ConcurrencyException(
                                                "Failed to acquire lock for run: " + runId.value()))
                                .flatMap(lock -> operation.get()
                                                .eventually(() -> {
                                                        // Only release lock if it was successfully acquired
                                                        if (lock != null) {
                                                                lockManager.releaseLock(lock);
                                                        }
                                                }))
                                .onFailure(TimeoutException.class)
                                .transform(ex -> new ConcurrencyException(
                                                "Failed to acquire lock for run: " + runId.value()))
                                .onFailure().recoverWithUni(failure -> {
                                        LOG.error("Operation failed for run: {} - {}", runId.value(),
                                                        failure.getMessage());
                                        return Uni.createFrom().failure(failure);
                                });
        }

        /**
         * Load workflow run with tenant validation
         */
        private Uni<WorkflowRun> loadRun(WorkflowRunId runId, TenantId tenantId) {
                return validateTenantAccess(tenantId)
                                .flatMap(v -> repository.findById(runId, tenantId))
                                .onItem().ifNull()
                                .failWith(() -> new NoSuchElementException("Workflow run not found: " + runId.value()));
        }

        /**
         * Load run for update (event sourcing pattern)
         */
        private Uni<WorkflowRun> loadRunForUpdate(WorkflowRunId runId) {
                return repository.findById(runId)
                                .flatMap(snapshot -> {
                                        if (snapshot == null) {
                                                return Uni.createFrom().failure(
                                                                new NoSuchElementException(
                                                                                "Run not found: " + runId.value()));
                                        }

                                        // Reconstruct from events
                                        return eventStore.getEvents(runId)
                                                        .flatMap(events -> definitionRegistry.getDefinition(
                                                                        snapshot.getDefinitionId(),
                                                                        snapshot.getTenantId())
                                                                        .map(definition -> WorkflowRun.fromEvents(
                                                                                        runId,
                                                                                        snapshot.getTenantId(),
                                                                                        definition,
                                                                                        events)));
                                });
        }

        /**
         * Persist workflow run (snapshot + events)
         */
        private Uni<WorkflowRun> persistRun(WorkflowRun run) {
                // Save snapshot
                return repository.save(run)
                                .flatMap(saved -> {
                                        // Save events
                                        List<ExecutionEvent> events = run.getUncommittedEvents();
                                        if (events.isEmpty()) {
                                                return Uni.createFrom().item(saved);
                                        }

                                        return eventStore.appendEvents(run.getId(), events, run.getVersion())
                                                        .map(v -> {
                                                                run.markEventsAsCommitted();
                                                                return saved;
                                                        });
                                });
        }

        /**
         * Publish events to message broker
         */
        private Uni<WorkflowRun> publishEvents(WorkflowRun run, List<ExecutionEvent> events) {
                if (events == null || events.isEmpty()) {
                        return Uni.createFrom().item(run);
                }

                return scheduler.publishEvents(events)
                                .replaceWith(run)
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to publish events for run: {}",
                                                run.getId().value(), throwable));
        }

        /**
         * Schedule nodes that are ready to execute
         */
        private Uni<WorkflowRun> scheduleReadyNodes(WorkflowRun run) {
                List<NodeId> pendingNodes = run.getPendingNodes();

                if (pendingNodes == null || pendingNodes.isEmpty()) {
                        return Uni.createFrom().item(run);
                }

                LOG.debug("Scheduling {} nodes for run: {}", pendingNodes.size(), run.getId().value());

                // Use collectFailures instead of andFailFast to allow partial success
                // This way, if one node fails to schedule, others can still proceed
                return Uni.join().all(
                                pendingNodes.stream()
                                                .map(nodeId -> scheduleNodeExecution(run, nodeId))
                                                .toList())
                                .collectFailures()
                                .onItem().transformToUni(failedUnis -> {
                                        if (!failedUnis.failed().isEmpty()) {
                                                LOG.warn("Some node scheduling failed for run: {}, failures: {}",
                                                                run.getId().value(), failedUnis.failed().size());
                                                // Log individual failures with more context
                                                failedUnis.failed()
                                                                .forEach(throwable -> LOG.warn(
                                                                                "Node scheduling failed for run {}: {}",
                                                                                run.getId().value(),
                                                                                throwable.getMessage()));
                                        }
                                        return Uni.createFrom().item(run);
                                })
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to schedule nodes for run: {}",
                                                run.getId().value(), throwable));
        }

        /**
         * Schedule individual node execution
         */
        private Uni<Void> scheduleNodeExecution(WorkflowRun run, NodeId nodeId) {
                NodeExecution execution = run.getNodeExecution(nodeId);

                return createExecutionToken(run.getId(), nodeId, execution.getAttempt())
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to create execution token for run: {}, node: {}",
                                                run.getId().value(), nodeId.value(), throwable))
                                .flatMap(token -> {
                                        NodeExecutionTask task = new NodeExecutionTask(
                                                        run.getId(),
                                                        nodeId,
                                                        execution.getAttempt(),
                                                        token,
                                                        run.getContext().getVariables());

                                        // Schedule the task and explicitly type the result
                                        Uni<Void> scheduleResult = scheduler.scheduleTask(task)
                                                        .onFailure()
                                                        .invoke(throwable -> LOG.error(
                                                                        "Failed to schedule task for run: {}, node: {}",
                                                                        run.getId().value(), nodeId.value(),
                                                                        throwable));

                                        // Chain the next operation
                                        return scheduleResult.flatMap(v -> {
                                                run.startNode(nodeId, execution.getAttempt());
                                                List<ExecutionEvent> events = run.getUncommittedEvents();
                                                return persistRun(run)
                                                                .flatMap(persisted -> publishEvents(persisted, events))
                                                                .replaceWithVoid();
                                        });
                                });
        }

        /**
         * Cancel all scheduled nodes
         */
        private Uni<Void> cancelScheduledNodes(WorkflowRun run) {
                return scheduler.cancelTasksForRun(run.getId())
                                .onFailure()
                                .invoke(throwable -> LOG.error("Failed to cancel scheduled tasks for run: {}",
                                                run.getId().value(), throwable));
        }

        /**
         * Initiate compensation if needed
         */
        private Uni<Void> initiateCompensationIfNeeded(WorkflowRun run) {
                if (run.getStatus() == RunStatus.COMPENSATING) {
                        return compensationCoordinator.compensate(run)
                                        .onFailure().invoke(throwable -> LOG.error("Compensation failed for run: {}",
                                                        run.getId().value(), throwable));
                }
                return Uni.createFrom().voidItem();
        }

        /**
         * Validate tenant access
         */
        private Uni<Void> validateTenantAccess(TenantId tenantId) {
                if (tenantId == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("TenantId cannot be null"));
                }
                if (tenantId.value() == null || tenantId.value().trim().isEmpty()) {
                        return Uni.createFrom().failure(
                                        new IllegalArgumentException("TenantId value cannot be null or empty"));
                }

                // Sanitize tenant ID to prevent injection attacks
                String sanitizedTenantId = tenantId.value().trim();
                if (!sanitizedTenantId.matches("^[a-zA-Z0-9_-]+$")) {
                        LOG.warn("Invalid tenant ID format: {}", sanitizedTenantId);
                        return Uni.createFrom().failure(new IllegalArgumentException("Invalid tenant ID format"));
                }

                return tenantContext.validateAccess(TenantId.of(sanitizedTenantId));
        }

        /**
         * Verify executor signature
         */
        private Uni<Boolean> verifyExecutorSignature(String signature) {
                // In a real implementation, this would validate the signature against a known
                // secret
                // For now, we'll implement a more robust check - in production, use proper
                // cryptographic verification
                if (signature == null || signature.trim().isEmpty()) {
                        LOG.warn("Null or empty executor signature provided");
                        return Uni.createFrom().item(false);
                }

                // Sanitize the signature to prevent injection attacks
                String sanitizedSignature = signature.trim();

                // Perform basic validation checks
                if (sanitizedSignature.length() < 10) {
                        LOG.warn("Executor signature too short: {}", sanitizedSignature.length());
                        return Uni.createFrom().item(false);
                }

                // Check for potentially dangerous characters
                if (!sanitizedSignature.matches("^[a-zA-Z0-9+/=]+$")) {
                        LOG.warn("Executor signature contains invalid characters");
                        return Uni.createFrom().item(false);
                }

                // This is a placeholder implementation - in a real system, you would:
                // 1. Have a registry of valid executors with their public keys or shared
                // secrets
                // 2. Verify the signature cryptographically
                // 3. Possibly check against a whitelist of known executors
                // For now, we'll just check that the signature has a reasonable format
                boolean isValid = sanitizedSignature.length() >= 10 && sanitizedSignature.length() <= 256;
                if (!isValid) {
                        LOG.warn("Executor signature length validation failed: {}", sanitizedSignature.length());
                }

                return Uni.createFrom().item(isValid);
        }

        /**
         * Verify callback token
         */
        private Uni<Boolean> verifyCallbackToken(WorkflowRunId runId, String token) {
                return repository.validateCallback(runId, token);
        }
}
