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
 * Shared resources across nodes
 */
public class SharedResources {
    public Map<String, String> schemas = new HashMap<>(); // name -> path
    public Map<String, String> widgets = new HashMap<>(); // name -> path
    public Map<String, String> scripts = new HashMap<>(); // name -> path
    public Map<String, Object> constants = new HashMap<>(); // Shared constants
}
