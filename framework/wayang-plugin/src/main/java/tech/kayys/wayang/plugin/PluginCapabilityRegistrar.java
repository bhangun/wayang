package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityRegistry;
import tech.kayys.wayang.spi.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PluginCapabilityRegistrar {

    private final CapabilityRegistry registry;
    private final PluginCapabilityDiscovery discovery;

    public PluginCapabilityRegistrar(CapabilityRegistry registry) {
        this(registry, new PluginCapabilityDiscovery());
    }

    public PluginCapabilityRegistrar(
            CapabilityRegistry registry,
            PluginCapabilityDiscovery discovery) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.discovery = Objects.requireNonNull(discovery, "discovery cannot be null");
    }

    public List<Capability> register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        CapabilityDiscoveryResult result = discovery.discover(plugin);
        List<Capability> registered = new ArrayList<>();

        if (registry instanceof DefaultCapabilityRegistry defaultRegistry) {
            try {
                for (Capability capability : result.capabilities()) {
                    defaultRegistry.register(capability.id(), plugin.id(), capability);
                    registered.add(capability);
                }
            } catch (Exception e) {
                for (Capability rolledBack : registered) {
                    defaultRegistry.unregister(rolledBack.id(), plugin.id());
                }
                throw e;
            }
        }

        return List.copyOf(registered);
    }

    public void unregister(Plugin plugin) {
        if (plugin == null) {
            return;
        }

        if (registry instanceof DefaultCapabilityRegistry defaultRegistry) {
            defaultRegistry.unregisterAll(plugin.id());
        }
    }
}
