package tech.kayys.wayang.communication.core.task;

import tech.kayys.wayang.communication.api.AgentError;
import tech.kayys.wayang.communication.api.AgentEvent;
import tech.kayys.wayang.communication.api.AgentResult;
import tech.kayys.wayang.communication.task.TaskStatus;

import java.util.function.Consumer;

public interface TaskController {

    void updateStatus(TaskStatus status);

    void publishEvent(AgentEvent event);

    void complete(AgentResult result);

    void fail(AgentError error);

    void onCancel(Runnable cancelHandler);
}
