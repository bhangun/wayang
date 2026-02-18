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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * UI Widget Definition - What widgets support
 */
public class UIWidgetDefinition {
    public String widgetId; // "form.password", "node.standard"
    public String type; // "react", "vue", "webcomponent"
    public Set<String> supportedDataTypes = new HashSet<>();
    public Set<String> capabilities = new HashSet<>();
    public String entryPoint; // Module path or CDN URL
    public String version;
    public Map<String, Object> defaultProps = new HashMap<>();
}
