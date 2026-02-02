package tech.kayys.wayang.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.gamelan.engine.workflow.WorkflowRun;
import tech.kayys.gamelan.engine.run.CreateRunRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gamelan-backed implementation of Wayang Workflow Run Management
 */
@ApplicationScoped
public class GamelanWorkflowRunManager {

    private final GamelanClient client;

    @Inject
    public GamelanWorkflowRunManager(AbstractGamelanWorkflowEngine engine) {
        this.client = engine.client();
    }

    /**
     * Create a new workflow run
     */
    public Uni<RunResponse> createRun(String workflowId, Map<String, Object> inputs) {
        var builder = client.runs().create(workflowId);
        if (inputs != null) {
            inputs.forEach(builder::input);
        }
        return builder.execute();
    }

    /**
     * Get a workflow run
     */
    public Uni<RunResponse> getRun(String runId) {
        return client.runs().get(runId);
    }

    /**
     * Resume a workflow run
     */
    public Uni<RunResponse> resumeRun(String runId, String humanTaskId, Map<String, Object> resumeData) {
        var builder = client.runs().resume(runId);
        if (humanTaskId != null) {
            builder.humanTaskId(humanTaskId);
        }
        if (resumeData != null) {
            resumeData.forEach(builder::input);
        }
        return builder.execute();
    }

    /**
     * Cancel a workflow run
     */
    public Uni<Void> cancelRun(String runId, String reason) {
        return client.runs().cancel(runId);
    }
}
