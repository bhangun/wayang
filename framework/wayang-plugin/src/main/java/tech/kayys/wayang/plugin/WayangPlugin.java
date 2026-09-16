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

import java.util.List;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginContext;
import tech.kayys.wayang.spi.plugin.PluginState;

/**
 * Developer-facing base contract for Wayang plugins.
 *
 * <p>The canonical plugin lifecycle contract remains
 * {@link Plugin}. This interface adds convenient identity
 * methods commonly needed by plugin implementations.</p>
 */
public interface WayangPlugin extends Plugin {

    /**
     * Human-readable plugin name.
     */
    String name();

    /**
     * Plugin version.
     */
    String version();

    /**
     * Plugin description.
     */
    String description();

    /**
     * Default manifest is intentionally derived from the plugin identity.
     */
    @Override
    default Manifest manifest() {
        return PluginManifests.basic(
                id(),
                name(),
                version(),
                description(),
                getClass().getName(),
                classLoader()
        );
    }

    /**
     * Default classloader is the classloader that loaded the plugin.
     */
    @Override
    default ClassLoader classLoader() {
        return getClass().getClassLoader();
    }

    /**
     * A simple plugin has no extensions unless it explicitly contributes them.
     */
    @Override
    default List<Extension> extensions() {
        return List.of();
    }

    /**
     * Default initial state.
     */
    @Override
    default PluginState state() {
        return PluginState.LOADED;
    }

    /**
     * New context-aware lifecycle hook.
     */
    @Override
    default void initialize(PluginContext context) throws Exception {
    }

    /**
     * Legacy lifecycle hook remains available through Plugin.
     */
    @Override
    default void initialize() throws Exception {
    }

    @Override
    default void start() throws Exception {
    }

    @Override
    default void stop() throws Exception {
    }
}
