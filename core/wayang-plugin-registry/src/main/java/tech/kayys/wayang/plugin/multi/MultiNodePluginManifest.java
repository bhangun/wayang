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

package tech.kayys.wayang.plugin.multi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.plugin.SharedResources;
import tech.kayys.wayang.plugin.executor.ExecutorManifest;
import tech.kayys.wayang.plugin.node.NodeManifest;

/**
 * Complete plugin manifest for multiple nodes
 */
public class MultiNodePluginManifest {

    // Plugin metadata
    public String pluginId;
    public String name;
    public String version;
    public String family; // "ai", "database", "http", "vector"
    public String author;
    public String description;

    // Multiple node definitions
    public List<NodeManifest> nodes = new ArrayList<>();

    // Multiple executors (optional - can share one executor)
    public List<ExecutorManifest> executors = new ArrayList<>();

    // Shared resources
    public SharedResources shared = new SharedResources();

    // Configuration
    public Map<String, Object> config = new HashMap<>();
}
