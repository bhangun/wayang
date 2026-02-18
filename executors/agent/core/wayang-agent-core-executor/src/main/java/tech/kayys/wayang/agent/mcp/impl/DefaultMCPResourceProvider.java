package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.mcp.MCPResourceProvider;
import tech.kayys.wayang.agent.mcp.model.MCPResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of MCPResourceProvider.
 * Provides access to file-based and in-memory resources.
 */
@ApplicationScoped
public class DefaultMCPResourceProvider implements MCPResourceProvider {

    private final Map<String, MCPResource> resources = new ConcurrentHashMap<>();
    private final Map<String, String> resourceContent = new ConcurrentHashMap<>();

    @Override
    public Uni<List<MCPResource>> listResources() {
        return Uni.createFrom().item(() -> List.copyOf(resources.values()));
    }

    @Override
    public Uni<String> readResource(String uri) {
        return Uni.createFrom().item(() -> {
            // Check in-memory resources first
            if (resourceContent.containsKey(uri)) {
                return resourceContent.get(uri);
            }

            // Try to read from file system if URI is a file path
            if (uri.startsWith("file://")) {
                try {
                    Path path = Paths.get(uri.substring(7));
                    return Files.readString(path);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read resource: " + uri, e);
                }
            }

            throw new IllegalArgumentException("Resource not found: " + uri);
        });
    }

    @Override
    public Uni<byte[]> readResourceBytes(String uri) {
        return Uni.createFrom().item(() -> {
            if (uri.startsWith("file://")) {
                try {
                    Path path = Paths.get(uri.substring(7));
                    return Files.readAllBytes(path);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read resource: " + uri, e);
                }
            }

            // For in-memory resources, convert string to bytes
            if (resourceContent.containsKey(uri)) {
                return resourceContent.get(uri).getBytes();
            }

            throw new IllegalArgumentException("Resource not found: " + uri);
        });
    }

    @Override
    public Subscription subscribe(String uri, ResourceUpdateHandler handler) {
        // Simplified implementation - in production, use file watchers
        return () -> {
        }; // No-op cancel
    }

    @Override
    public Uni<Boolean> resourceExists(String uri) {
        return Uni.createFrom().item(() -> {
            if (resources.containsKey(uri)) {
                return true;
            }

            if (uri.startsWith("file://")) {
                Path path = Paths.get(uri.substring(7));
                return Files.exists(path);
            }

            return false;
        });
    }

    /**
     * Register an in-memory resource.
     */
    public void registerResource(MCPResource resource, String content) {
        resources.put(resource.getUri(), resource);
        resourceContent.put(resource.getUri(), content);
    }

    /**
     * Register a file-based resource.
     */
    public void registerFileResource(String filePath, String name, String description) {
        String uri = "file://" + filePath;
        MCPResource resource = MCPResource.builder()
                .uri(uri)
                .name(name)
                .description(description)
                .mimeType(guessMimeType(filePath))
                .build();
        resources.put(uri, resource);
    }

    private String guessMimeType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".json"))
            return "application/json";
        if (lower.endsWith(".xml"))
            return "application/xml";
        if (lower.endsWith(".yaml") || lower.endsWith(".yml"))
            return "application/yaml";
        if (lower.endsWith(".md"))
            return "text/markdown";
        if (lower.endsWith(".txt"))
            return "text/plain";
        return "application/octet-stream";
    }
}
