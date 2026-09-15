package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.task.TaskId;
import tech.kayys.wayang.communication.task.TaskStatus;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public interface AgentTask {

    TaskId id();

    TaskStatus status();

    AgentRef agent();

    CompletionStage<AgentResult> result();

    Flow.Publisher<AgentEvent> events();

    CompletionStage<Void> cancel();
}
