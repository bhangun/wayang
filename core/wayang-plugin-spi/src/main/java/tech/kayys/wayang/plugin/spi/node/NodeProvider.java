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

package tech.kayys.wayang.plugin.spi.node;

import java.util.List;

/**
 * SPI for modules to contribute fully-described workflow nodes at runtime.
 *
 * <p>
 * Each implementation returns one or more {@link NodeDefinition}s that
 * describe the node's metadata, schemas (config / input / output), and
 * default configuration.
 * </p>
 *
 * <p>
 * Implementations are discovered via {@link java.util.ServiceLoader}.
 * The orchestrator and schema catalog both consume this SPI to build
 * the unified node registry.
 * </p>
 *
 * <h3>Example</h3>
 * 
 * <pre>{@code
 * public class MyNodeProvider implements NodeProvider {
 *     &#64;Override
 *     public List<NodeDefinition> nodes() {
 *         return List.of(
 *             new NodeDefinition("my-node", "My Node", "Custom", ...));
 *     }
 * }
 * }</pre>
 */
public interface NodeProvider {

    /**
     * @return an immutable list of node definitions contributed by this module.
     */
    List<NodeDefinition> nodes();
}
