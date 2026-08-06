package tech.kayys.wayang.configuration;
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
 * Configuration Source
 */
public enum ConfigSource {
    FILE,
    ENVIRONMENT,
    SYSTEM_PROPERTIES,
    DATABASE,
    NETWORK,
    RUNTIME,
    MEMORY,
    PLUGIN,
    TENANT,
    DEFAULT,
    MERGED,
    UNKNOWN
}