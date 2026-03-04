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

import tech.kayys.gamelan.engine.node.NodeDefinition;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.node.NodeType;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.wayang.orchestrator.spi.WayangOrchestratorSpi;
import tech.kayys.wayang.schema.WayangSpec;
import tech.kayys.wayang.schema.canvas.CanvasNode;

import java.util.List;
import java.util.Map;

public abstract class AbstractGamelanWorkflowEngine implements WayangOrchestratorSpi, AutoCloseable {

    protected GamelanClient client;
    protected GamelanEngineConfig config;

    /**
     * Required for CDI proxying
     */
    protected AbstractGamelanWorkflowEngine() {
        this.config = null;
    }

    protected AbstractGamelanWorkflowEngine(GamelanEngineConfig config) {
        this.config = config;
    }

    public GamelanClient client() {
        return client;
    }

    @Override
    public Uni<String> deploy(String name, WayangSpec spec) {
        var builder = client.workflows().create(name)
                .description("Wayang Deployed Definition")
                .version(spec.getSpecVersion() != null ? spec.getSpecVersion() : "1.0.0")
                .tenantId("default"); // Replace with actual tenant context if needed

        // Map Wayang canvas nodes to Gamelan nodes
        if (spec.getCanvas() != null && spec.getCanvas().nodes != null) {
            for (CanvasNode node : spec.getCanvas().nodes) {
                // Create Gamelan node definition based on Wayang canvas
                NodeDefinition gNode = NodeDefinition.builder()
                        .id(NodeId.of(node.id))
                        .name(node.config != null && node.config.containsKey("label")
                                ? node.config.get("label").toString()
                                : (node.label != null ? node.label : node.id))
                        .type(NodeType.EXECUTOR) // Default to EXECUTOR for Wayang nodes
                        // Gamelan executor type must match the Wayang node type
                        .executorType(node.type)
                        .configuration(node.config)
                        .build();

                builder.addNode(gNode);
            }
        }

        // Map Wayang connections to Gamelan dependencies (logic omitted for brevity,
        // handled by subclass if needed)

        // Deploy Workflow Definition to Gamelan and return its native ID
        return builder.execute().map(workflowDef -> workflowDef.id().value());
    }

    @Override
    public Uni<String> run(String definitionId, Map<String, Object> inputs) {
        var builder = client.runs().create(definitionId);
        if (inputs != null) {
            inputs.forEach(builder::input);
        }
        return builder.execute().map(RunResponse::getRunId);
    }

    @Override
    public Uni<String> getStatus(String executionId) {
        return client.runs().get(executionId)
                .map(run -> run.getStatus() != null ? run.getStatus() : "UNKNOWN");
    }

    @Override
    public Uni<Boolean> stop(String executionId) {
        return client.runs().cancel(executionId, "Stopped via Wayang").replaceWith(true);
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
