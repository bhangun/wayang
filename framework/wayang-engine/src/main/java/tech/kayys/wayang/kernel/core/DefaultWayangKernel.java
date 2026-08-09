package tech.kayys.wayang.kernel.core;

package org.wayang.kernel.core;

import org.wayang.kernel.api.*;
import org.wayang.kernel.core.event.DefaultEventBus;
import org.wayang.kernel.core.registry.DefaultCapabilityRegistry;
import org.wayang.kernel.core.registry.DefaultRuntimeRegistry;
import org.wayang.kernel.core.registry.DefaultServiceRegistry;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Default implementation of WayangKernel.
 * 
 * <p>
 * This is the foundation of the entire Wayang platform.
 * It composes and manages all subsystems, providing:
 * <ul>
 * <li>Runtime lifecycle management</li>
 * <li>Service discovery and dependency injection</li>
 * <li>Capability management</li>
 * <li>Plugin loading and management</li>
 * <li>Event-driven communication</li>
 * <li>Configuration management</li>
 * </ul>
 */
@ApplicationScoped
public final class DefaultWayangKernel implements WayangKernel {

    private static final Logger LOGGER = Logger.getLogger(DefaultWayangKernel.class.getName());

    private final KernelId id;
    private final String name;
    private final Version version;
    private final RuntimeRegistry runtimeRegistry;
    private final ServiceRegistry serviceRegistry;
    private final CapabilityRegistry capabilityRegistry;
    private final PluginManager pluginManager;
    private final EventBus eventBus;
    private final Configuration configuration;
    private final ResourceManager resourceManager;
    private final Clock clock;
    private final ExecutorService executorService;
    private volatile KernelState state;
    private final DefaultWayangKernel parent;
    private final List<WayangKernel> children;
    private final KernelMetrics metrics;

    /**
     * CDI Constructor
     */
    @Inject
    public DefaultWayangKernel(
            RuntimeRegistry runtimeRegistry,
            ServiceRegistry serviceRegistry,
            CapabilityRegistry capabilityRegistry,
            PluginManager pluginManager,
            EventBus eventBus,
            Configuration configuration,
            ResourceManager resourceManager) {
        this.id = KernelId.generate();
        this.name = "root";
        this.version = Version.parse("1.0.0");
        this.parent = null;
        this.children = new CopyOnWriteArrayList<>();
        this.state = KernelState.CREATED;
        this.clock = Clock.systemUTC();
        
        this.runtimeRegistry = runtimeRegistry;
        this.serviceRegistry = serviceRegistry;
        this.capabilityRegistry = capabilityRegistry;
        this.pluginManager = pluginManager;
        this.eventBus = eventBus;
        this.configuration = configuration;
        this.resourceManager = resourceManager;
        this.executorService = Executors.newCachedThreadPool();
        this.metrics = new KernelMetrics();
        
        registerCoreServices();
        LOGGER.info("Created CDI kernel: " + name + " (version " + version + ")");
    }

    public DefaultWayangKernel() {
        // Required for proxying by CDI
        this.id = null;
        this.name = null;
        this.version = null;
        this.parent = null;
        this.children = null;
        this.state = null;
        this.clock = null;
        this.runtimeRegistry = null;
        this.serviceRegistry = null;
        this.capabilityRegistry = null;
        this.pluginManager = null;
        this.eventBus = null;
        this.configuration = null;
        this.resourceManager = null;
        this.executorService = null;
        this.metrics = null;
    }

    public DefaultWayangKernel(DefaultWayangKernel parent, String name, Version version) {
        this.id = KernelId.generate();
        this.name = name;
        this.version = version;
        this.parent = parent;
        this.children = new CopyOnWriteArrayList<>();
        this.state = KernelState.CREATED;
        this.clock = Clock.systemUTC();

        // Create registries
        this.runtimeRegistry = new DefaultRuntimeRegistry();
        this.serviceRegistry = new DefaultServiceRegistry();
        this.capabilityRegistry = new DefaultCapabilityRegistry();
        this.pluginManager = new DefaultPluginManager();
        this.eventBus = new DefaultEventBus();
        this.configuration = new DefaultConfiguration();
        this.resourceManager = new DefaultResourceManager();
        this.executorService = Executors.newCachedThreadPool();
        this.metrics = new KernelMetrics();

        // Register core services
        registerCoreServices();

        LOGGER.info("Created kernel: " + name + " (version " + version + ")");
    }

    @Override
    public RuntimeRegistry runtimes() {
        return runtimeRegistry;
    }

    @Override
    public ServiceRegistry services() {
        return serviceRegistry;
    }

    @Override
    public CapabilityRegistry capabilities() {
        return capabilityRegistry;
    }

    @Override
    public PluginManager plugins() {
        return pluginManager;
    }

    @Override
    public EventBus events() {
        return eventBus;
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public ResourceManager resources() {
        return resourceManager;
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public KernelState state() {
        return state;
    }

    @Override
    public Version version() {
        return version;
    }

    @Override
    public synchronized void start() {
        if (state == KernelState.RUNNING) {
            LOGGER.warning("Kernel already running");
            return;
        }

        LOGGER.info("Starting kernel: " + name);
        state = KernelState.STARTING;

        try {
            // Start plugins
            for (Plugin plugin : pluginManager.plugins()) {
                if (plugin.state() == PluginState.INITIALIZED) {
                    plugin.start();
                }
            }

            // Start runtimes in dependency order
            List<Runtime> sortedRuntimes = sortByDependencies(runtimeRegistry.runtimes());
            for (Runtime runtime : sortedRuntimes) {
                if (runtime.state() != RuntimeState.REGISTERED &&
                        runtime.state() != RuntimeState.INITIALIZED) {
                    continue;
                }
                LOGGER.fine("Starting runtime: " + runtime.name());
                runtime.start();
            }

            state = KernelState.RUNNING;
            LOGGER.info("Kernel started: " + name);

            // Emit event
            eventBus.publish(new KernelStartedEvent(this));

        } catch (Exception e) {
            LOGGER.severe("Failed to start kernel: " + e.getMessage());
            state = KernelState.ERROR;
            throw new RuntimeException("Kernel startup failed", e);
        }
    }

    @Override
    public synchronized void stop() {
        if (state == KernelState.STOPPED) {
            return;
        }

        LOGGER.info("Stopping kernel: " + name);
        state = KernelState.STOPPING;

        try {
            // Stop runtimes in reverse order
            List<Runtime> sortedRuntimes = sortByDependencies(runtimeRegistry.runtimes());
            for (int i = sortedRuntimes.size() - 1; i >= 0; i--) {
                Runtime runtime = sortedRuntimes.get(i);
                if (runtime.state().isActive()) {
                    LOGGER.fine("Stopping runtime: " + runtime.name());
                    runtime.stop();
                }
            }

            // Stop plugins
            for (Plugin plugin : pluginManager.plugins()) {
                if (plugin.state() == PluginState.ACTIVE) {
                    plugin.stop();
                }
            }

            state = KernelState.STOPPED;
            LOGGER.info("Kernel stopped: " + name);

            // Emit event
            eventBus.publish(new KernelStoppedEvent(this));

        } catch (Exception e) {
            LOGGER.severe("Failed to stop kernel: " + e.getMessage());
            state = KernelState.ERROR;
            throw new RuntimeException("Kernel shutdown failed", e);
        }
    }

    @Override
    public boolean isRunning() {
        return state == KernelState.RUNNING;
    }

    @Override
    public KernelMetrics metrics() {
        return metrics;
    }

    @Override
    public WayangKernel createChild(String name) {
        DefaultWayangKernel child = new DefaultWayangKernel(this, name, version);
        children.add(child);
        LOGGER.info("Created child kernel: " + name);
        return child;
    }

    @Override
    public KernelContext context() {
        return new DefaultKernelContext(this);
    }

    private void registerCoreServices() {
        // Register kernel itself as a service
        serviceRegistry.register(WayangKernel.class, this);
        serviceRegistry.register(KernelContext.class, context());

        // Register core services
        serviceRegistry.register(Clock.class, clock);
        serviceRegistry.register(EventBus.class, eventBus);
        serviceRegistry.register(Configuration.class, configuration);
        serviceRegistry.register(ResourceManager.class, resourceManager);

        LOGGER.fine("Registered core services");
    }

    private List<Runtime> sortByDependencies(Collection<Runtime> runtimes) {
        // Simple topological sort
        List<Runtime> sorted = new ArrayList<>();
        Set<RuntimeId> visited = new HashSet<>();

        for (Runtime runtime : runtimes) {
            if (!visited.contains(runtime.id())) {
                visit(runtime, runtimes, visited, sorted);
            }
        }

        return sorted;
    }

    private void visit(Runtime runtime, Collection<Runtime> all,
            Set<RuntimeId> visited, List<Runtime> sorted) {
        if (visited.contains(runtime.id())) {
            return;
        }

        visited.add(runtime.id());

        // Visit dependencies first
        for (RuntimeId depId : runtime.dependencies()) {
            all.stream()
                    .filter(r -> r.id().equals(depId))
                    .findFirst()
                    .ifPresent(dep -> visit(dep, all, visited, sorted));
        }

        sorted.add(runtime);
    }
}