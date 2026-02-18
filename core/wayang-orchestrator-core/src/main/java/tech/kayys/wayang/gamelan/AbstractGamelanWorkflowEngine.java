/*
 * PolyForm Noncommercial License 1.0.0
 *
 * Copyright (c) 2026 Kayys.tech
 *
 * This software is licensed for non-commercial use only.
 * You may use, modify, and distribute this software for personal,
 * educational, or research purposes.
 *
 * Commercial use, including SaaS or revenue-generating services,
 * requires a separate commercial license from Kayys.tech.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 *
 * @author Bhangun
 */

package tech.kayys.wayang.gamelan;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PreDestroy;
import tech.kayys.gamelan.sdk.client.GamelanClient;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.wayang.integration.designer.RouteDesign;
import tech.kayys.wayang.integration.designer.DesignNode;
import tech.kayys.wayang.integration.designer.DesignConnection;
import tech.kayys.gamelan.engine.node.NodeDefinition;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.node.NodeType;

import java.util.List;
import java.util.Map;

/**
 * Abstract base class for Gamelan-based Workflow Engine.
 * Handles common logic for deployment and transport-agnostic client usage.
 */
public abstract class AbstractGamelanWorkflowEngine implements AutoCloseable {

    protected GamelanClient client;
    protected final GamelanEngineConfig config;

    protected AbstractGamelanWorkflowEngine(GamelanEngineConfig config) {
        this.config = config;
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
                .version(routeDesign.metadata() != null ? routeDesign.metadata().version() : "1.0.0")
                .tenantId(routeDesign.tenantId());

        // Map Wayang nodes to Gamelan nodes
        if (routeDesign.nodes() != null) {
            for (DesignNode node : routeDesign.nodes()) {
                // Create Gamelan node definition
                NodeDefinition gNode = NodeDefinition.builder()
                        .id(NodeId.of(node.nodeId()))
                        .name(node.label() != null ? node.label() : node.nodeId())
                        .type(NodeType.EXECUTOR) // Default to EXECUTOR for Wayang nodes
                        .executorType(node.nodeType())
                        .configuration(node.configuration())
                        .build();

                builder.addNode(gNode);
            }
        }

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

    @PreDestroy
    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
