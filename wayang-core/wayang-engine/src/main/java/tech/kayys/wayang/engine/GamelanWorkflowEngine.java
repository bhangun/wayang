package tech.kayys.wayang.engine;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.sdk.client.GamelanClientConfig;
import tech.kayys.gamelan.sdk.client.TransportType;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.wayang.integration.designer.RouteDesign;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gamelan-based implementation of Wayang Workflow Engine
 */
public class GamelanWorkflowEngine implements AutoCloseable {

    private final GamelanClient client;
    private final GamelanEngineConfig config;

    public GamelanWorkflowEngine(GamelanEngineConfig config) {
        this.config = config;
        this.client = GamelanClient.builder()
                .restEndpoint(config.endpoint())
                .tenantId(config.tenantId())
                .apiKey(config.apiKey())
                .timeout(config.timeout())
                .build();
    }

    /**
     * Deploy a Wayang Route Design as a Gamelan Workflow Definition
     */
    public Uni<WorkflowDefinition> deploy(RouteDesign routeDesign) {
        var builder = client.workflows().create(routeDesign.name())
                .description(routeDesign.description())
                .tenantId(routeDesign.tenantId());

        // TODO: Map Wayang nodes to Gamelan nodes
        // This requires a mapping logic between Wayang DSL and Gamelan engine internal model
        
        return builder.execute();
    }

    /**
     * Start a workflow run
     */
    public Uni<RunResponse> startRun(String workflowDefinitionId) {
        return client.runs().create(workflowDefinitionId).execute();
    }

    /**
     * List all workflow definitions
     */
    public Uni<List<WorkflowDefinition>> listWorkflows() {
        return client.workflows().list();
    }

    @Override
    public void close() {
        client.close();
    }
}
