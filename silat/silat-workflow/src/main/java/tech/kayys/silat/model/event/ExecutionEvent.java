package tech.kayys.silat.model.event;

import java.time.Instant;

import tech.kayys.silat.model.WorkflowRunId;

/**
 * Execution Event - Event sourcing events
 */
public sealed interface ExecutionEvent permits
        WorkflowStartedEvent,
        NodeScheduledEvent,
        NodeStartedEvent,
        NodeCompletedEvent,
        NodeFailedEvent,
        WorkflowSuspendedEvent,
        WorkflowResumedEvent,
        WorkflowCompletedEvent,
        WorkflowFailedEvent,
        WorkflowCancelledEvent,
        GenericExecutionEvent {

    String eventId();

    WorkflowRunId runId();

    Instant occurredAt();

    String eventType();
}
