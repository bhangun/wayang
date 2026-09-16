package tech.kayys.wayang.spi.plugin;

import java.util.List;
import tech.kayys.wayang.extension.Extension;

/**
 * Wayang plugin lifecycle contract.
 *
 * <p>The no-argument initialize() method is retained for
 * backward compatibility. New plugins should prefer
 * initialize(PluginContext).</p>
 */
public interface Plugin {

    String id();

    Manifest manifest();

    PluginState state();

    ClassLoader classLoader();

    List<Extension> extensions();

    /**
     * Legacy initialization hook.
     *
     * @deprecated implement initialize(PluginContext) for new plugins.
     */
    @Deprecated(forRemoval = false)
    default void initialize() throws Exception {
    }

    /**
     * Initializes the plugin with its runtime context.
     *
     * <p>The default implementation delegates to the legacy
     * no-argument initialize() method, preserving compatibility
     * with existing plugins.</p>
     */
    default void initialize(PluginContext context) throws Exception {
        initialize();
    }

    void start() throws Exception;

    void stop() throws Exception;
}
