package tech.kayys.wayang.plugin.service;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import tech.kayys.wayang.plugin.dto.AgentPlugin;
import tech.kayys.wayang.plugin.dto.ConnectorPlugin;
import tech.kayys.wayang.plugin.dto.Plugin;
import tech.kayys.wayang.plugin.dto.PluginType;

import org.jboss.logging.Logger;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginRegistryService {

    private static final Logger LOG = Logger.getLogger(PluginRegistryService.class);

    // Core registries
    private final Map<String, PluginDescriptor> pluginDescriptors = new ConcurrentHashMap<>();
    private final Map<String, Plugin> pluginInstances = new ConcurrentHashMap<>();
    private final Map<PluginType, List<PluginDescriptor>> pluginsByType = new ConcurrentHashMap<>();

    @Inject
    PluginLoader pluginLoader;

    @Inject
    PluginStorageService storageService;

    @Inject
    PluginDependencyResolver dependencyResolver;

    @Inject
    PluginSecurityManager securityManager;

    @Inject
    PluginMetricsCollector metrics;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Plugin Registry Service");

        // Load all installed plugins
        loadAllPlugins()
                .subscribe().with(
                        count -> LOG.infof("Loaded %d plugins", count),
                        failure -> LOG.error("Failed to load plugins", failure));
    }

    @PostConstruct
    void init() {
        // Initialize registries
        for (PluginType type : PluginType.values()) {
            pluginsByType.put(type, new CopyOnWriteArrayList<>());
        }
    }

    @PreDestroy
    void shutdown() {
        LOG.info("Shutting down Plugin Registry Service");

        // Destroy all plugin instances
        destroyAllPlugins()
                .subscribe().with(
                        () -> LOG.info("All plugins destroyed"),
                        failure -> LOG.error("Error during shutdown", failure));
    }

    // ========================================================================
    // PLUGIN REGISTRATION & LOADING
    // ========================================================================

    @NonBlocking
    public Uni<PluginRegistrationResult> registerPlugin(Path pluginPath) {
        return Uni.createFrom().deferred(() -> {
            // 1. Load and validate plugin
            return pluginLoader.loadPlugin(pluginPath)
                    .chain(plugin ->
            // 2. Validate dependencies
            dependencyResolver.resolve(plugin.getDescriptor())
                    .chain(resolution -> resolution.isResolved()
                            ? registerPluginInternal(plugin)
                            : Uni.createFrom().failure(
                                    new DependencyResolutionException(resolution.getUnresolved()))))
                    .map(plugin -> PluginRegistrationResult.success(plugin.getDescriptor()))
                    .onFailure().recoverWithItem(failure -> PluginRegistrationResult.failure(failure.getMessage()));
        });
    }

    private Uni<Plugin> registerPluginInternal(Plugin plugin) {
        PluginDescriptor descriptor = plugin.getDescriptor();
        String pluginKey = getPluginKey(descriptor);

        return Uni.createFrom().deferred(() -> {
            // Check if already registered
            if (pluginDescriptors.containsKey(pluginKey)) {
                return Uni.createFrom().failure(new PluginAlreadyRegisteredException(
                        descriptor.id(), descriptor.version()));
            }

            // 1. Store descriptor
            pluginDescriptors.put(pluginKey, descriptor);

            // 2. Add to type index
            pluginsByType.get(descriptor.type()).add(descriptor);

            // 3. Initialize plugin if auto-load enabled
            if (descriptor.metadata().getOrDefault("autoLoad", false)) {
                return initializePlugin(plugin)
                        .map(initialized -> {
                            pluginInstances.put(pluginKey, initialized);
                            return initialized;
                        });
            } else {
                pluginInstances.put(pluginKey, plugin);
                return Uni.createFrom().item(plugin);
            }
        })
                .onItem().invoke(registered -> {
                    metrics.recordPluginRegistered(descriptor);
                    LOG.infof("Plugin registered: %s v%s", descriptor.id(), descriptor.version());
                });
    }

    @NonBlocking
    public Uni<Plugin> loadPlugin(String pluginId, SemanticVersion version) {
        return Uni.createFrom().deferred(() -> {
            String pluginKey = getPluginKey(pluginId, version);

            // Check if already loaded
            Plugin loaded = pluginInstances.get(pluginKey);
            if (loaded != null) {
                return Uni.createFrom().item(loaded);
            }

            // Get descriptor
            PluginDescriptor descriptor = pluginDescriptors.get(pluginKey);
            if (descriptor == null) {
                return Uni.createFrom().failure(new PluginNotFoundException(pluginId, version));
            }

            // Load plugin instance
            return pluginLoader.loadPlugin(descriptor.pluginPath())
                    .chain(this::initializePlugin)
                    .onItem().invoke(initialized -> {
                        pluginInstances.put(pluginKey, initialized);
                        metrics.recordPluginLoaded(descriptor);
                    });
        });
    }

    private Uni<Plugin> initializePlugin(Plugin plugin) {
        return Uni.createFrom().deferred(() -> {
            PluginContext context = createPluginContext(plugin.getDescriptor());

            return plugin.initialize(context)
                    .onItem().transform(v -> plugin)
                    .onFailure().recoverWithUni(failure -> Uni.createFrom().failure(new PluginInitializationException(
                            plugin.getDescriptor().id(), failure)));
        });
    }

    // ========================================================================
    // PLUGIN DISCOVERY & QUERY
    // ========================================================================

    @NonBlocking
    public Uni<List<PluginDescriptor>> findPlugins(PluginQuery query) {
        return Uni.createFrom().item(() -> {
            return pluginDescriptors.values().stream()
                    .filter(descriptor -> matchesQuery(descriptor, query))
                    .sorted(Comparator.comparing(PluginDescriptor::name))
                    .collect(Collectors.toList());
        });
    }

    @NonBlocking
    public Multi<PluginDescriptor> streamPlugins(PluginQuery query) {
        return Multi.createFrom().iterable(pluginDescriptors.values())
                .filter(descriptor -> matchesQuery(descriptor, query))
                .onOverflow().buffer(100);
    }

    @NonBlocking
    public Uni<Optional<PluginDescriptor>> getPlugin(String pluginId, SemanticVersion version) {
        return Uni.createFrom().item(() -> Optional.ofNullable(pluginDescriptors.get(getPluginKey(pluginId, version))));
    }

    @NonBlocking
    public Uni<Optional<Plugin>> getPluginInstance(String pluginId, SemanticVersion version) {
        return Uni.createFrom().item(() -> Optional.ofNullable(pluginInstances.get(getPluginKey(pluginId, version))));
    }

    // ========================================================================
    // PLUGIN EXECUTION ROUTING
    // ========================================================================

    @NonBlocking
    public Uni<ExecutionResult> executePlugin(String pluginId, SemanticVersion version,
            Map<String, Object> inputs) {
        return getPluginInstance(pluginId, version)
                .onItem().ifNull().failWith(() -> new PluginNotFoundException(pluginId, version))
                .chain(plugin -> routeToRuntime(plugin, inputs));
    }

    private Uni<ExecutionResult> routeToRuntime(Plugin plugin, Map<String, Object> inputs) {
        // Route to appropriate runtime service based on plugin type
        return switch (plugin.getType()) {
            case AGENT -> executeAgentPlugin((AgentPlugin) plugin, inputs);
            case CONNECTOR -> executeConnectorPlugin((ConnectorPlugin) plugin, inputs);
            case TRANSFORMER -> executeTransformerPlugin((TransformerPlugin) plugin, inputs);
            case KNOWLEDGE -> executeKnowledgePlugin((KnowledgePlugin) plugin, inputs);
            case HITL -> executeHITLPlugin((HITLPlugin) plugin, inputs);
            default -> executeGenericPlugin(plugin, inputs);
        };
    }

    private Uni<ExecutionResult> executeAgentPlugin(AgentPlugin agent, Map<String, Object> inputs) {
        return Uni.createFrom().deferred(() -> {
            AgentContext context = AgentContext.fromInputs(inputs);

            return agent.execute(context)
                    .map(result -> ExecutionResult.success(result.toMap()))
                    .onFailure().recoverWithItem(failure -> ExecutionResult.failure(failure));
        });
    }

    private Uni<ExecutionResult> executeConnectorPlugin(ConnectorPlugin connector,
            Map<String, Object> inputs) {
        return Uni.createFrom().deferred(() -> {
            ConnectionConfig config = ConnectionConfig.fromMap(
                    (Map<String, Object>) inputs.get("config"));

            return connector.connect(config)
                    .chain(connection -> {
                        String operation = (String) inputs.get("operation");
                        return executeConnectorOperation(connection, operation, inputs);
                    })
                    .map(result -> ExecutionResult.success(result))
                    .onFailure().recoverWithItem(failure -> ExecutionResult.failure(failure));
        });
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private boolean matchesQuery(PluginDescriptor descriptor, PluginQuery query) {
        if (query.getType() != null && descriptor.type() != query.getType()) {
            return false;
        }

        if (query.getStatus() != null && descriptor.status() != query.getStatus()) {
            return false;
        }

        if (!query.getTags().isEmpty() &&
                !descriptor.tags().containsAll(query.getTags())) {
            return false;
        }

        if (query.getCapability() != null &&
                !descriptor.capabilities().contains(query.getCapability())) {
            return false;
        }

        return true;
    }

    private PluginContext createPluginContext(PluginDescriptor descriptor) {
        return new PluginContext(
                descriptor.id(),
                descriptor.version(),
                descriptor.metadata(),
                securityManager.createSecurityContext(descriptor),
                metrics.getPluginMetrics(descriptor.id()));
    }

    private String getPluginKey(PluginDescriptor descriptor) {
        return getPluginKey(descriptor.id(), descriptor.version());
    }

    private String getPluginKey(String pluginId, SemanticVersion version) {
        return pluginId + "@" + version;
    }

    private Uni<Integer> loadAllPlugins() {
        return storageService.loadAllDescriptors()
                .onItem().transformToMulti(descriptors -> Multi.createFrom().iterable(descriptors))
                .onItem().transformToUniAndConcatenate(this::registerDescriptor)
                .collect().asList()
                .map(List::size);
    }

    private Uni<Void> registerDescriptor(PluginDescriptor descriptor) {
        return Uni.createFrom().deferred(() -> {
            pluginDescriptors.put(getPluginKey(descriptor), descriptor);
            pluginsByType.get(descriptor.type()).add(descriptor);
            return Uni.createFrom().voidItem();
        });
    }

    private Uni<Void> destroyAllPlugins() {
        return Multi.createFrom().iterable(pluginInstances.values())
                .onItem().transformToUniAndConcatenate(Plugin::destroy)
                .collect().asList()
                .replaceWithVoid();
    }
}
