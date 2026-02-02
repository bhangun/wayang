package tech.kayys.wayang.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.wayang.integration.designer.RouteDesign;
import tech.kayys.wayang.integration.designer.DesignNode;
import tech.kayys.wayang.integration.designer.DesignConnection;

import java.util.List;
import java.util.Map;

/**
 * Gamelan-based implementation of Wayang Workflow Engine
 */
@ApplicationScoped
public class GamelanWorkflowEngine implements AutoCloseable {

    private final GamelanClient client;
    private final GamelanEngineConfig config;

    @Inject
    public GamelanWorkflowEngine(GamelanEngineConfig config) {
        this.config = config;
        this.client = GamelanClient.builder()
                .restEndpoint(config.endpoint())
                .tenantId(config.tenantId())
                .apiKey(config.apiKey().orElse(null))
                .timeout(config.timeout())
                .build();
    }

    public GamelanClient client() {
        return client;
    }

    /**
     * Deploy a Wayang Route Design as a Gamelan Workflow Definition
     */
    public Uni<WorkflowDefinition> deploy(RouteDesign routeDesign) {
        var builder = client.workflows().create(routeDesign.name() != null ? routeDesign.name() : routeDesign.routeId())
                .description(routeDesign.description())
                .tenantId(routeDesign.tenantId());

        // Map Wayang nodes to Gamelan nodes
        if (routeDesign.nodes() != null) {
            for (DesignNode node : routeDesign.nodes()) {
                // Determine start node
                if (isStartNode(node, routeDesign.connections())) {
                    builder.startNode(node.nodeId());
                }

                // Create Gamelan node
                // Assuming Gamelan SDK builder has methods for adding nodes
                // For now, we use high-level execute() call which we'll refine
            }
        }

        // Map connections
        if (routeDesign.connections() != null) {
            for (DesignConnection conn : routeDesign.connections()) {
                // builder.connect(conn.sourceNodeId(), conn.targetNodeId(), conn.condition());
            }
        }

        return builder.execute();
    }

    private boolean isStartNode(DesignNode node, List<DesignConnection> connections) {
        if (connections == null)
            return true;
        return connections.stream().noneMatch(c -> c.targetNodeId().equals(node.nodeId()));
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

    @PreDestroy
    @Override
    public void close() {
        client.close();
    }
}
