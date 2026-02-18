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

import java.util.Map;

public class LoadedPlugin {
    public final ClassLoader classLoader;
    public final Map<String, Class<?>> classes;
    public final Map<String, Object> instances;
    public final Map<String, byte[]> resources;

    public LoadedPlugin(
            ClassLoader classLoader,
            Map<String, Class<?>> classes,
            Map<String, Object> instances,
            Map<String, byte[]> resources) {
        this.classLoader = classLoader;
        this.classes = classes;
        this.instances = instances;
        this.resources = resources;
    }
}
