/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2026 Kayys.tech
 */
package tech.kayys.wayang.plugin.node;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Lightweight runtime descriptor for a node within an execution contract.
 * Carries only the fields needed to route and trace a single execution request.
 */
@RegisterForReflection
public class NodeDescriptor {

    /** Unique node-type identifier (matches {@link NodeDefinition#type}). */
    public String type;

    /** Semantic version of the node at the time of execution. */
    public String version;

    /** Per-invocation instance identifier supplied by the orchestrator. */
    public String instanceId;
}
