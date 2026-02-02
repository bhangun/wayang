package tech.kayys.wayang.plugin.executor;

import tech.kayys.wayang.plugin.WayangPlugin;
import java.util.Set;

/**
 * Plugin that provides one or more executors.
 */
public interface ExecutorPlugin extends WayangPlugin {
    /**
     * Returns the set of capabilities provided by this executor plugin.
     */
    Set<String> capabilities();
}
