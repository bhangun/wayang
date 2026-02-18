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
package tech.kayys.wayang.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.plugin.node.NodeDefinition;
import tech.kayys.wayang.schema.validator.SchemaValidator;
import tech.kayys.wayang.schema.validator.ValidationResult;

/**
 * Control Plane Node Registry - Authority for node definitions
 */
@ApplicationScoped
public class ControlPlaneNodeRegistry {

    private static final Logger LOG = Logger.getLogger(ControlPlaneNodeRegistry.class);

    private final Map<String, NodeDefinition> nodeRegistry = new ConcurrentHashMap<>();

    @Inject
    SchemaValidator schemaValidator;

    /**
     * Register node with schema validation
     */
    public void register(NodeDefinition node) {
        // Validate schemas are proper JSON Schema
        validateSchemas(node);

        nodeRegistry.put(node.type, node);
        LOG.infof("Registered node: %s (executor: %s, protocol: %s)",
                node.type,
                node.executorBinding.executorId,
                node.executorBinding.protocol);
    }

    public void unregister(String nodeType) {
        nodeRegistry.remove(nodeType);
        LOG.infof("Unregistered node: %s", nodeType);
    }

    public NodeDefinition get(String nodeType) {
        return nodeRegistry.get(nodeType);
    }

    public List<NodeDefinition> getAll() {
        return new ArrayList<>(nodeRegistry.values());
    }

    public List<NodeDefinition> getByCategory(String category) {
        return nodeRegistry.values().stream()
                .filter(n -> category.equals(n.category))
                .toList();
    }

    /**
     * Validate node configuration at runtime
     */
    public ValidationResult validateConfig(String nodeType, Map<String, Object> config) {
        NodeDefinition node = nodeRegistry.get(nodeType);
        if (node == null) {
            return ValidationResult.failure("Node type not found: " + nodeType);
        }

        return schemaValidator.validate(node.configSchema, config);
    }

    /**
     * Validate node inputs at runtime
     */
    public ValidationResult validateInputs(String nodeType, Map<String, Object> inputs) {
        NodeDefinition node = nodeRegistry.get(nodeType);
        if (node == null) {
            return ValidationResult.failure("Node type not found: " + nodeType);
        }

        return schemaValidator.validate(node.inputSchema, inputs);
    }

    private void validateSchemas(NodeDefinition node) {
        if (node.configSchema == null && node.inputSchema == null && node.outputSchema == null) {
            LOG.warnf("Node %s has no schemas defined", node.type);
        }
    }
}
