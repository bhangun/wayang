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
package tech.kayys.wayang.plugin.node;

import java.util.HashMap;
import java.util.Map;

import tech.kayys.wayang.schema.validator.SchemaReference;

/**
 * Node manifest entry
 */
public class NodeManifest {
    public String type;
    public String label;
    public String category;
    public String subCategory;
    public String description;
    public String icon;
    public String color;

    // Schema references (can be inline or file paths)
    public SchemaReference configSchema;
    public SchemaReference inputSchema;
    public SchemaReference outputSchema;

    // Executor binding
    public String executorId;

    // UI binding
    public String widgetId;

    // Node-specific config
    public Map<String, Object> config = new HashMap<>();
}