package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityProvider;
import tech.kayys.wayang.spi.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

public final class PluginCapabilityDiscovery {

    public CapabilityDiscoveryResult discover(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        List<Capability> discovered = new ArrayList<>();

        if (plugin instanceof CapabilityProvider provider) {
            List<Capability> caps = provider.capabilities();
            if (caps != null) {
                discovered.addAll(caps);
            }
        }

        ClassLoader cl = plugin.classLoader();
        if (cl != null) {
            ServiceLoader<CapabilityProvider> loader = ServiceLoader.load(CapabilityProvider.class, cl);
            for (CapabilityProvider provider : loader) {
                if (provider != null && provider.capabilities() != null) {
                    discovered.addAll(provider.capabilities());
                }
            }
        }

        return new CapabilityDiscoveryResult(plugin.id(), List.copyOf(discovered));
    }
}
