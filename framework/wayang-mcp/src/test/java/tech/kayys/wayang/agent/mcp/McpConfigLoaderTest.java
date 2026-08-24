package tech.kayys.wayang.agent.mcp;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.kayys.wayang.agent.spi.skills.SkillRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpConfigLoaderTest {

    @Test
    void loadsBuiltinClasspathConfig() {
        McpConfigLoader loader = new McpConfigLoader();
        Map<String, McpServerConfig> configs = loader.loadMergedConfigs();

        assertThat(configs).containsKey("flutter");
        McpServerConfig flutter = configs.get("flutter");
        assertThat(flutter.command()).isEqualTo("flutter");
        assertThat(flutter.args()).containsExactly("mcp-server");
    }

    @Test
    void customPathProviderSuppliesPluginMcpConfig(@TempDir Path customDir) throws IOException {
        String pluginJson = """
            {
              "mcpServers": {
                "aljabr-custom": {
                  "command": "aljabr-cli",
                  "args": ["mcp"],
                  "enabled": true
                }
              }
            }
            """;
        Path customMcpFile = customDir.resolve("mcp.json");
        Files.writeString(customMcpFile, pluginJson);

        McpConfigPathProvider provider = () -> List.of(customMcpFile);
        McpConfigLoader loader = new McpConfigLoader(null, List.of(provider));

        Map<String, McpServerConfig> configs = loader.loadMergedConfigs();
        assertThat(configs).containsKey("aljabr-custom");
        assertThat(configs.get("aljabr-custom").command()).isEqualTo("aljabr-cli");
    }

    @Test
    void workspaceConfigOverridesBuiltinAndAddsNew(@TempDir Path workspaceDir) throws IOException {
        String workspaceJson = """
            {
              "mcpServers": {
                "flutter": {
                  "command": "fvm",
                  "args": ["flutter", "mcp-server"],
                  "enabled": true
                },
                "sqlite": {
                  "command": "uvx",
                  "args": ["mcp-server-sqlite", "--db-path", "test.db"],
                  "enabled": true
                }
              }
            }
            """;
        Files.writeString(workspaceDir.resolve(".mcp.json"), workspaceJson);

        McpConfigLoader loader = new McpConfigLoader();
        Map<String, McpServerConfig> configs = loader.loadMergedConfigs(workspaceDir);

        assertThat(configs).containsKeys("flutter", "sqlite", "git");
        // Flutter is overridden by workspace config
        McpServerConfig flutter = configs.get("flutter");
        assertThat(flutter.command()).isEqualTo("fvm");
        assertThat(flutter.args()).containsExactly("flutter", "mcp-server");

        // Sqlite is newly added
        McpServerConfig sqlite = configs.get("sqlite");
        assertThat(sqlite.command()).isEqualTo("uvx");
    }

    @Test
    void reloadSynchronizesBridgeConnections(@TempDir Path workspaceDir) throws IOException {
        FakeSkillRegistry registry = new FakeSkillRegistry();
        FakeToolClient client = new FakeToolClient();
        McpSkillBridge bridge = new McpSkillBridge(client, registry);
        McpConfigLoader loader = new McpConfigLoader(bridge, List.of());

        String workspaceJson = """
            {
              "mcpServers": {
                "custom-mcp": {
                  "command": "node",
                  "args": ["custom.js"],
                  "enabled": true
                }
              }
            }
            """;
        Files.writeString(workspaceDir.resolve(".mcp.json"), workspaceJson);

        List<McpToolDescriptor> tools = loader.reload(workspaceDir)
            .await().atMost(Duration.ofSeconds(2));

        assertThat(tools).isNotEmpty();
        assertThat(loader.currentConfigs()).containsKey("custom-mcp");
    }

    private static final class FakeToolClient implements McpToolClient {
        @Override
        public Uni<List<McpToolDescriptor>> listTools(McpServerConfig server) {
            return Uni.createFrom().item(List.of(
                new McpToolDescriptor(server.id(), "sample_tool", "A sample tool", Map.of(), Map.of())
            ));
        }

        @Override
        public Uni<McpToolCallResult> callTool(McpToolInvocation invocation) {
            return Uni.createFrom().item(McpToolCallResult.success(Map.of("result", "ok"), 5));
        }
    }

    private static final class FakeSkillRegistry implements SkillRegistry {
        private final Map<String, tech.kayys.wayang.agent.spi.AgentSkill> skills = new java.util.HashMap<>();
        @Override public List<tech.kayys.wayang.agent.spi.AgentSkill> listAll() { return List.copyOf(skills.values()); }
        @Override public java.util.Optional<tech.kayys.wayang.agent.spi.AgentSkill> find(String id) { return java.util.Optional.ofNullable(skills.get(id)); }
        @Override public tech.kayys.wayang.agent.spi.AgentSkill findOrThrow(String id) { return skills.get(id); }
        @Override public void register(tech.kayys.wayang.agent.spi.AgentSkill skill) { skills.put(skill.id(), skill); }
        @Override public void unregister(String skillId) { skills.remove(skillId); }
        @Override public List<tech.kayys.wayang.agent.spi.AgentSkill> findByCategory(tech.kayys.wayang.agent.spi.skills.SkillCategory category) { return List.of(); }
        @Override public List<tech.kayys.wayang.agent.spi.AgentSkill> listAllowed(String tenantId, java.util.Set<String> allowedIds) { return List.of(); }
        @Override public boolean isRegistered(String skillId) { return skills.containsKey(skillId); }
        @Override public Map<String, tech.kayys.wayang.agent.spi.skills.SkillHealth> checkHealth() { return Map.of(); }
        @Override public int size() { return skills.size(); }
        @Override public java.util.Optional<tech.kayys.wayang.agent.spi.skills.SkillDefinition> getSkill(String skillId) { return java.util.Optional.empty(); }
        @Override public List<tech.kayys.wayang.agent.spi.skills.SkillDefinition> listSkills() { return List.of(); }
        @Override public List<tech.kayys.wayang.agent.spi.skills.SkillDefinition> listByCategory(String category) { return List.of(); }
        @Override public void registerSkill(tech.kayys.wayang.agent.spi.skills.SkillDefinition skill) {}
        @Override public boolean unregisterSkill(String skillId) { return false; }
    }
}
