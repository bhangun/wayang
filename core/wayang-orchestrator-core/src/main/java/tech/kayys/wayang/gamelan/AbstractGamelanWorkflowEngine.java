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
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractGamelanWorkflowEngine implements WayangOrchestratorSpi, AutoCloseable {
    private static final java.util.Set<String> SOURCE_TRIGGER_TYPES = java.util.Set.of(
            "start",
            "trigger-manual",
            "trigger-schedule",
            "trigger-email",
            "trigger-telegram",
            "trigger-websocket",
            "trigger-webhook",
            "trigger-event",
            "trigger-kafka",
            "trigger-file");
    private static final String SOURCE_TRIGGER_EXECUTOR = "trigger-source-executor";

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
                String nodeName = node.config != null && node.config.containsKey("label")
                        ? node.config.get("label").toString()
                        : (node.label != null ? node.label : node.id);
                String executorType = resolveExecutorType(node);
                Map<String, Object> configuration = buildNodeConfiguration(node, nodeName, executorType);

                // Create Gamelan node definition based on Wayang canvas
                NodeDefinition gNode = NodeDefinition.builder()
                        .id(NodeId.of(node.id))
                        .name(nodeName)
                        .type(NodeType.EXECUTOR) // Default to EXECUTOR for Wayang nodes
                        .executorType(executorType)
                        .configuration(configuration)
                        .build();

                builder.addNode(gNode);
            }
        }

        // Map Wayang connections to Gamelan dependencies (logic omitted for brevity,
        // handled by subclass if needed)

        // Deploy Workflow Definition to Gamelan and return its native ID
        return builder.execute().map(workflowDef -> workflowDef.id().value());
    }

    private static String resolveExecutorType(CanvasNode node) {
        if (node.type != null && !node.type.isBlank()) {
            if (SOURCE_TRIGGER_TYPES.contains(node.type)) {
                return SOURCE_TRIGGER_EXECUTOR;
            }
            return node.type;
        }
        if (node.subType != null && !node.subType.isBlank()) {
            return node.subType;
        }
        return node.id;
    }

    private static Map<String, Object> buildNodeConfiguration(CanvasNode node, String nodeName, String executorType) {
        Map<String, Object> configuration = new LinkedHashMap<>();
        if (node.config != null) {
            configuration.putAll(node.config);
        }

        // __node_type__ must stay as the original canvas node type for executor canHandle().
        String nodeType = resolveNodeType(node, executorType);
        configuration.put("__node_type__", nodeType);
        configuration.putIfAbsent("__executor_type__", executorType);
        configuration.putIfAbsent("__node_id__", node.id);
        if (nodeName != null && !nodeName.isBlank()) {
            configuration.putIfAbsent("__node_label__", nodeName);
        }

        return configuration;
    }

    private static String resolveNodeType(CanvasNode node, String fallback) {
        if (node.type != null && !node.type.isBlank()) {
            return node.type;
        }
        if (node.subType != null && !node.subType.isBlank()) {
            return node.subType;
        }
        return fallback;
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
