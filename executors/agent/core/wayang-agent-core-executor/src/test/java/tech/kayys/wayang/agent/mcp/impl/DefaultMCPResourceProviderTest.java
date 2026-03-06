package tech.kayys.wayang.agent.mcp.impl;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.agent.mcp.model.MCPResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMCPResourceProviderTest {

    @Test
    void inMemoryResourceIsListedAndReadable() {
        DefaultMCPResourceProvider provider = new DefaultMCPResourceProvider();
        MCPResource resource = MCPResource.builder()
                .uri("memory://workflow/spec")
                .name("Workflow Spec")
                .description("Spec content")
                .build();

        provider.registerResource(resource, "{\"name\":\"test\"}");

        List<MCPResource> resources = provider.listResources().await().indefinitely();
        String content = provider.readResource("memory://workflow/spec").await().indefinitely();
        boolean exists = provider.resourceExists("memory://workflow/spec").await().indefinitely();

        assertEquals(1, resources.size());
        assertEquals("memory://workflow/spec", resources.get(0).getUri());
        assertEquals("{\"name\":\"test\"}", content);
        assertTrue(exists);
    }

    @Test
    void fileResourceCanBeReadAsStringAndBytes() throws Exception {
        DefaultMCPResourceProvider provider = new DefaultMCPResourceProvider();
        Path tempFile = Files.createTempFile("mcp-resource-", ".txt");
        Files.writeString(tempFile, "hello-mcp", StandardCharsets.UTF_8);

        String uri = "file://" + tempFile;
        String content = provider.readResource(uri).await().indefinitely();
        byte[] bytes = provider.readResourceBytes(uri).await().indefinitely();
        boolean exists = provider.resourceExists(uri).await().indefinitely();

        assertEquals("hello-mcp", content);
        assertArrayEquals("hello-mcp".getBytes(StandardCharsets.UTF_8), bytes);
        assertTrue(exists);
    }
}
