package tech.kayys.wayang.agent.mcp;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and merges MCP configurations from:
 * 1. Classpath built-in resources: `resources/mcp/mcp.json` or `/mcp/mcp.json`
 * 2. Platform default user config: `~/.wayang/mcp/mcp.json`
 * 3. Registered {@link McpConfigPathProvider} extensions (e.g. Aljabr provider)
 * 4. Optional workspace override: `.mcp.json` in workspace root.
 *
 * Provides a dynamic {@link #reload()} function to hot-reload and synchronize
 * active MCP server connections at runtime without restarting.
 */
@ApplicationScoped
public class McpConfigLoader {

    private static final Logger LOG = Logger.getLogger(McpConfigLoader.class);

    private static final String CLASSPATH_BUILTIN_MCP = "mcp/mcp.json";
    private static final Path DEFAULT_WAYANG_MCP = Paths.get(System.getProperty("user.home"), ".wayang", "mcp", "mcp.json");

    private final McpSkillBridge skillBridge;
    private final List<McpConfigPathProvider> pathProviders;
    private final Map<String, McpServerConfig> currentConfigs = new ConcurrentHashMap<>();

    @Inject
    public McpConfigLoader(McpSkillBridge skillBridge, Instance<McpConfigPathProvider> customProviders) {
        this.skillBridge = skillBridge;
        this.pathProviders = customProviders != null ? customProviders.stream().toList() : List.of();
    }

    public McpConfigLoader(McpSkillBridge skillBridge, List<McpConfigPathProvider> customProviders) {
        this.skillBridge = skillBridge;
        this.pathProviders = customProviders != null ? List.copyOf(customProviders) : List.of();
    }

    public McpConfigLoader() {
        this(null, List.of());
    }

    /**
     * Reads and merges all MCP configuration tiers (built-in + user/SPI providers + workspace).
     */
    public Map<String, McpServerConfig> loadMergedConfigs(Path workspaceRoot) {
        Map<String, McpServerConfig> merged = new LinkedHashMap<>();

        // Tier 1: Classpath built-in resources (resources/mcp/mcp.json)
        loadFromClasspath().forEach(cfg -> merged.put(cfg.id(), cfg));

        // Tier 2: Platform user home directory (~/.wayang/mcp/mcp.json)
        loadFromPath(DEFAULT_WAYANG_MCP).forEach(cfg -> merged.put(cfg.id(), cfg));

        // Tier 2b: Custom SPI path providers (e.g. Aljabr config provider)
        if (pathProviders != null) {
            for (McpConfigPathProvider provider : pathProviders) {
                List<Path> paths = provider.userConfigPaths();
                if (paths != null) {
                    for (Path path : paths) {
                        loadFromPath(path).forEach(cfg -> merged.put(cfg.id(), cfg));
                    }
                }
            }
        }

        // Tier 3: Optional workspace-local .mcp.json
        if (workspaceRoot != null) {
            Path wsMcp = workspaceRoot.resolve(".mcp.json");
            loadFromPath(wsMcp).forEach(cfg -> merged.put(cfg.id(), cfg));
        }

        return merged;
    }

    public Map<String, McpServerConfig> loadMergedConfigs() {
        return loadMergedConfigs(null);
    }

    /**
     * Hot-reloads MCP configurations from disk and synchronizes active MCP connections.
     * Connects newly added or updated servers and disconnects removed servers.
     */
    public Uni<List<McpToolDescriptor>> reload(Path workspaceRoot) {
        Map<String, McpServerConfig> newConfigs = loadMergedConfigs(workspaceRoot);
        LOG.infof("Reloading MCP configurations: found %d servers", newConfigs.size());

        Set<String> previousIds = new HashSet<>(currentConfigs.keySet());
        Set<String> newIds = newConfigs.keySet();

        // Disconnect servers that have been removed
        List<Uni<Void>> disconnects = previousIds.stream()
            .filter(id -> !newIds.contains(id))
            .map(id -> {
                LOG.infof("Disconnecting removed MCP server: %s", id);
                currentConfigs.remove(id);
                return skillBridge != null ? skillBridge.disconnect(id) : Uni.createFrom().voidItem();
            })
            .toList();

        Uni<Void> disconnectAll = disconnects.isEmpty()
            ? Uni.createFrom().voidItem()
            : Uni.combine().all().unis(disconnects).discardItems();

        return disconnectAll.chain(() -> {
            currentConfigs.clear();
            currentConfigs.putAll(newConfigs);

            if (skillBridge == null || newConfigs.isEmpty()) {
                return Uni.createFrom().item(List.of());
            }

            List<Uni<List<McpToolDescriptor>>> connects = newConfigs.values().stream()
                .map(skillBridge::connect)
                .toList();

            return Uni.combine().all().unis(connects).with(results -> {
                List<McpToolDescriptor> allTools = new ArrayList<>();
                for (Object res : results) {
                    if (res instanceof List<?> list) {
                        for (Object item : list) {
                            if (item instanceof McpToolDescriptor desc) {
                                allTools.add(desc);
                            }
                        }
                    }
                }
                return allTools;
            });
        });
    }

    public Uni<List<McpToolDescriptor>> reload() {
        return reload(null);
    }

    public Map<String, McpServerConfig> currentConfigs() {
        return Collections.unmodifiableMap(currentConfigs);
    }

    private List<McpServerConfig> loadFromClasspath() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CLASSPATH_BUILTIN_MCP)) {
            if (in != null) {
                String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                List<McpServerConfig> configs = McpServerConfig.parseMcpServersJson(json);
                LOG.infof("Loaded %d built-in MCP server(s) from classpath:%s", configs.size(), CLASSPATH_BUILTIN_MCP);
                return configs;
            }
        } catch (Exception e) {
            LOG.warnf("Failed to read built-in MCP config from classpath:%s - %s", CLASSPATH_BUILTIN_MCP, e.getMessage());
        }
        return List.of();
    }

    private List<McpServerConfig> loadFromPath(Path path) {
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            return List.of();
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<McpServerConfig> configs = McpServerConfig.parseMcpServersJson(json);
            LOG.infof("Loaded %d MCP server(s) from %s", configs.size(), path);
            return configs;
        } catch (Exception e) {
            LOG.warnf("Failed to read MCP config from %s: %s", path, e.getMessage());
            return List.of();
        }
    }
}
