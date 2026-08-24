package tech.kayys.wayang.agent.mcp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class McpServerConfigTest {

    @Test
    void parsesFlutterMcpServerJsonConfig() throws IOException {
        String json = """
            {
              "mcpServers": {
                "flutter": {
                  "command": "flutter",
                  "args": ["mcp-server"]
                },
                "git": {
                  "command": "npx",
                  "args": ["-y", "@modelcontextprotocol/server-github"]
                },
                "remote-agent": {
                  "url": "http://localhost:8080/mcp"
                }
              }
            }
            """;

        List<McpServerConfig> configs = McpServerConfig.parseMcpServersJson(json);
        assertThat(configs).hasSize(3);

        McpServerConfig flutter = configs.stream().filter(c -> c.id().equals("flutter")).findFirst().orElseThrow();
        assertThat(flutter.command()).isEqualTo("flutter");
        assertThat(flutter.args()).containsExactly("mcp-server");
        assertThat(flutter.transportType()).isEqualTo(McpTransportType.STDIO);
        assertThat(flutter.enabled()).isTrue();

        McpServerConfig remote = configs.stream().filter(c -> c.id().equals("remote-agent")).findFirst().orElseThrow();
        assertThat(remote.url()).isEqualTo("http://localhost:8080/mcp");
        assertThat(remote.transportType()).isEqualTo(McpTransportType.HTTP);
    }
}
