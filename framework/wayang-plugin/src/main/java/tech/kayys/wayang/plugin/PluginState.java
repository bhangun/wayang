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


/**
 * Plugin State
 */
public enum PluginState {
    LOADED,
    RESOLVED,
    STARTING,
    ACTIVE,
    STOPPING,
    STOPPED,
    ERROR
}

