package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.task.TaskId;

import java.time.Instant;
import java.util.Objects;

public record AgentEvent(
        TaskId taskId,
        AgentEventType type,
        Instant timestamp,
        Object data
) {

    public AgentEvent {
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(type, "type");
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public static AgentEvent of(TaskId taskId, AgentEventType type, Object data) {
        return new AgentEvent(taskId, type, Instant.now(), data);
    }

    public static AgentEvent submitted(TaskId taskId) {
        return of(taskId, AgentEventType.SUBMITTED, null);
    }

    public static AgentEvent started(TaskId taskId) {
        return of(taskId, AgentEventType.STARTED, null);
    }

    public static AgentEvent completed(TaskId taskId, Object result) {
        return of(taskId, AgentEventType.COMPLETED, result);
    }

    public static AgentEvent failed(TaskId taskId, Object error) {
        return of(taskId, AgentEventType.FAILED, error);
    }

    public static AgentEvent canceled(TaskId taskId) {
        return of(taskId, AgentEventType.CANCELED, null);
    }
}
