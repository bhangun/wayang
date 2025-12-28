package tech.kayys.wayang.plugin.service;

@ApplicationScoped
public class PluginRegistry {

    private final Map<PluginType, List<PluginDescriptor>> plugins = new ConcurrentHashMap<>();
    private final Map<String, NodeFactory> nodeFactories = new ConcurrentHashMap<>();
    private final Map<String, PatternFactory> patternFactories = new ConcurrentHashMap<>();

    @Inject
    PluginLoader pluginLoader;

    @Inject
    PluginDependencyResolver dependencyResolver;

    public Uni<Void> registerPlugin(PluginDescriptor descriptor) {
        return dependencyResolver.validateDependencies(descriptor)
                .chain(() -> {
                    // Register all components from this plugin
                    descriptor.getNodeTypes().forEach(this::registerNodeType);
                    descriptor.getPatterns().forEach(this::registerPattern);
                    descriptor.getConnectors().forEach(this::registerConnector);

                    plugins.computeIfAbsent(descriptor.getType(), k -> new ArrayList<>())
                            .add(descriptor);

                    log.info("Registered plugin: {} v{}",
                            descriptor.getName(), descriptor.getVersion());
                    return Uni.createFrom().voidItem();
                });
    }

    public Node createNode(String nodeType, NodeConfig config) {
        NodeFactory factory = nodeFactories.get(nodeType);
        if (factory == null) {
            throw new NodeTypeNotFoundException(nodeType);
        }
        return factory.create(config);
    }

    public WorkflowPattern createPattern(String patternType, PatternConfig config) {
        PatternFactory factory = patternFactories.get(patternType);
        if (factory == null) {
            throw new PatternNotFoundException(patternType);
        }
        return factory.create(config);
    }
}