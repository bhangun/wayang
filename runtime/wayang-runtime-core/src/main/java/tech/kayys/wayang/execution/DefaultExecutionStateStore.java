package tech.kayys.wayang.execution;

import tech.kayys.wayang.core.AgentResponse;
import tech.kayys.wayang.execution.event.EventLedger;
import tech.kayys.wayang.execution.event.ExecutionEvent;
import tech.kayys.wayang.execution.event.ExecutionEventType;
import tech.kayys.wayang.agent.AgentContext;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of ExecutionStateStore.
 */
public class DefaultExecutionStateStore implements ExecutionStateStore {

    private final CheckpointStore checkpointStore;
    private final EventLedger eventLedger;
    private final Map<String, AgentExecutionState> states = new ConcurrentHashMap<>();
    private final AtomicLong eventSeq = new AtomicLong();

    public DefaultExecutionStateStore(CheckpointStore checkpointStore, EventLedger eventLedger) {
        this.checkpointStore = checkpointStore;
        this.eventLedger = eventLedger;
    }

    @Override
    public AgentExecutionState get(String executionId) {
        return states.computeIfAbsent(executionId, id -> 
            new AgentExecutionState(id, ExecutionStatus.PENDING, ExecutionPhase.INPUT, 0, 0, null, null, null, 0, 0, Instant.now(), Instant.now())
        );
    }

    @Override
    public AgentExecutionState transition(String executionId, ExecutionPhase phase, Map<String, Object> metadata) {
        AgentExecutionState current = get(executionId);
        AgentExecutionState next = current.withPhase(phase);
        states.put(executionId, next);

        if (eventLedger != null) {
            ExecutionEventType eventType = mapPhaseToEvent(phase);
            if (eventType != null) {
                eventLedger.record(ExecutionEvent.of(executionId, eventSeq.getAndIncrement(), eventType, phase.name(), metadata != null ? metadata : Map.of()));
            }
        }
        return next;
    }

    @Override
    public void checkpoint(AgentExecutionState state) {
        // Here we'd save the state. For now, since CheckpointStore takes AgentContext, we'll leave it as a marker.
        // The actual context checkpointing happens at boundaries.
    }

    @Override
    public void complete(String executionId, AgentResponse response) {
        AgentExecutionState current = get(executionId);
        states.put(executionId, new AgentExecutionState(
            executionId, ExecutionStatus.COMPLETED, ExecutionPhase.COMPLETE,
            current.attempt(), current.iteration(), current.checkpointId(), current.lastEventId(),
            current.modelId(), current.inputTokens(), current.outputTokens(), current.startedAt(), Instant.now()
        ));
    }

    @Override
    public void fail(String executionId, Throwable error) {
        AgentExecutionState current = get(executionId);
        states.put(executionId, new AgentExecutionState(
            executionId, ExecutionStatus.FAILED, ExecutionPhase.COMPLETE,
            current.attempt(), current.iteration(), current.checkpointId(), current.lastEventId(),
            current.modelId(), current.inputTokens(), current.outputTokens(), current.startedAt(), Instant.now()
        ));
    }
    
    private ExecutionEventType mapPhaseToEvent(ExecutionPhase phase) {
        return switch(phase) {
            case INPUT -> ExecutionEventType.EXECUTION_STARTED;
            case CONTEXT -> ExecutionEventType.CONTEXT_COMPILED;
            case TOOL -> ExecutionEventType.TOOL_CALL_STARTED;
            case INFERENCE -> ExecutionEventType.MODEL_ROUTING_RESOLVED;
            default -> null;
        };
    }
}
