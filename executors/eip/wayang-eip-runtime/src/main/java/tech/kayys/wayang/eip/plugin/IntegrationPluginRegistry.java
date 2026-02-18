package tech.kayys.wayang.eip.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IntegrationPluginRegistry {

    private final Map<String, IntegrationPlugin> pluginsById = new ConcurrentHashMap<>();

    @Inject
    public IntegrationPluginRegistry(Instance<IntegrationPlugin> plugins) {
        for (IntegrationPlugin plugin : plugins) {
            String id = normalize(plugin.id());
            IntegrationPlugin previous = pluginsById.putIfAbsent(id, plugin);
            if (previous != null) {
                throw new IllegalStateException("Duplicate integration plugin id: " + plugin.id());
            }
        }
    }

    public Optional<IntegrationPlugin> find(String pluginId) {
        return Optional.ofNullable(pluginsById.get(normalize(pluginId)));
    }

    public List<IntegrationPlugin> all() {
        return pluginsById.values().stream()
                .sorted(Comparator.comparing(IntegrationPlugin::id))
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
