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
import java.util.Map;

/**
 * UI Reference - Points to widget, NEVER contains implementation
 */
public class UIReference {
    public String widgetId; // "form.password", "node.standard"
    public Map<String, Object> props = new HashMap<>();

    public UIReference() {
    }

    public UIReference(String widgetId) {
        this.widgetId = widgetId;
    }
}