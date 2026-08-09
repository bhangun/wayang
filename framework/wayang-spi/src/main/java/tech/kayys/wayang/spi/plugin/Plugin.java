package tech.kayys.wayang.spi.plugin;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.PluginState;

/**
 * Plugin
 */
public interface Plugin {
    String id();
    Manifest manifest();
    PluginState state();
    ClassLoader classLoader();
    List<Extension> extensions();
    void initialize() throws Exception;
    void start() throws Exception;
    void stop() throws Exception;
}


