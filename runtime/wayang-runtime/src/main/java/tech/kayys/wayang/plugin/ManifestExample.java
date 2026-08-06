package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.PluginManager;
import tech.kayys.wayang.spi.plugin.PluginState;
import tech.kayys.wayang.spi.plugin.ManifestRegistry;
import tech.kayys.wayang.spi.plugin.Dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.nio.file.*;
import java.util.*;

import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.core.Version;
import tech.kayys.wayang.spi.plugin.Dependency;
import tech.kayys.wayang.plugin.Manifest;
import tech.kayys.wayang.plugin.ManifestStatus;
import tech.kayys.wayang.plugin.ProvidedCapability;
import tech.kayys.wayang.plugin.RequiredCapability;

public class ManifestExample {
    
    public static void main(String[] args) throws Exception {
        
        // ============================================================================
        // 1. Create a manifest using the builder
        // ============================================================================
        
        Manifest manifest = Manifest.builder()
            .id("wayang-ai-plugin")
            .name("Wayang AI Plugin")
            .version("2.0.0")
            .description("AI capabilities for Wayang")
            .mainClass("io.wayang.plugin.AIPlugin")
            .author("Wayang Team")
            .author("Contributor")
            .license("Apache-2.0")
            .repository("https://github.com/wayang/ai-plugin")
            .documentation("https://docs.wayang.io/ai-plugin")
            
            // Dependencies
            .dependency("wayang-core", Version.VERSION_1_0_0)
            .dependency("wayang-database", Version.parse("1.5.0"))
            .dependency(Dependency.of("wayang-messaging", Version.parse("1.0.0"), "provided"))
            
            // Provided capabilities
            .provides(ProvidedCapability.of("llm-provider", "Wayang LLM Provider", "model"))
            .provides(new ProvidedCapability(
                "embedding-provider",
                "Wayang Embedding Provider",
                Version.parse("2.0.0"),
                "embedding",
                "Provides text embedding capabilities",
                List.of("text-embedding", "semantic-search"),
                Map.of("dimension", 768)
            ))
            
            // Required capabilities
            .requires(RequiredCapability.of("database", "database"))
            .requires(new RequiredCapability(
                "cache",
                Version.VERSION_1_0_0,
                "cache",
                true,
                "Cache provider is optional"
            ))
            
            // Permissions
            .permission(Permission.of("model:*", "invoke"))
            .permission(Permission.of("database:users", "read"))
            .permission(Permission.of("database:users", "write"))
            
            // Configuration
            .config("llm.defaultModel", "gpt-4")
            .config("llm.timeout", 30000)
            .config("llm.maxTokens", 2000)
            .config("embedding.dimension", 768)
            .config("retry.maxAttempts", 3)
            .config("retry.backoff", 1000)
            
            // Metadata
            .metadata("builtWith", "Java 17")
            .metadata("compatibleWith", "Wayang 1.0+")
            
            .status(ManifestStatus.PUBLISHED)
            .build();
        
        System.out.println("=== Manifest Created ===");
        System.out.println("ID: " + manifest.id().asString());
        System.out.println("Name: " + manifest.name());
        System.out.println("Version: " + manifest.version());
        System.out.println("Status: " + manifest.status());
        System.out.println("Dependencies: " + manifest.dependencies().size());
        System.out.println("Provides: " + manifest.provides().size());
        System.out.println("Requires: " + manifest.requires().size());
        System.out.println("Permissions: " + manifest.permissions().size());
        
        // ============================================================================
        // 2. Export to different formats
        // ============================================================================
        
        System.out.println("\n=== YAML ===");
        System.out.println(manifest.toYaml());
        
        System.out.println("\n=== JSON ===");
        System.out.println(manifest.toJson());
        
        System.out.println("\n=== Properties ===");
        System.out.println(manifest.toProperties());
        
        // ============================================================================
        // 3. Load from file
        // ============================================================================
        
        Path manifestPath = Paths.get("manifest.yaml");
        Files.writeString(manifestPath, manifest.toYaml());
        
        Manifest loadedManifest = DefaultManifest.fromFile(manifestPath);
        System.out.println("\n=== Loaded from File ===");
        System.out.println("Name: " + loadedManifest.name());
        System.out.println("Version: " + loadedManifest.version());
        System.out.println("Main Class: " + loadedManifest.mainClass());
        System.out.println("Authors: " + String.join(", ", loadedManifest.authors()));
        
        // ============================================================================
        // 4. Manifest Registry
        // ============================================================================
        
        ManifestRegistry registry = new DefaultManifestRegistry();
        registry.register(manifest);
        
        System.out.println("\n=== Registry ===");
        System.out.println("Total manifests: " + registry.getAll().size());
        System.out.println("By status: " + registry.getByStatus(ManifestStatus.PUBLISHED).size());
        System.out.println("By capability: " + registry.getByCapability("llm-provider").size());
        
        Optional<Manifest> found = registry.getByName("Wayang AI Plugin");
        if (found.isPresent()) {
            System.out.println("Found manifest: " + found.get().id().asString());
        }
        
        // ============================================================================
        // 5. Scan a directory for manifests
        // ============================================================================
        
        Path configDir = Paths.get("config");
        Files.createDirectories(configDir);
        
        // Write another manifest
        Path anotherPath = configDir.resolve("another-plugin.yaml");
        Manifest anotherManifest = Manifest.builder()
            .id("another-plugin")
            .name("Another Plugin")
            .version("1.0.0")
            .description("Another plugin for Wayang")
            .author("Community")
            .license("MIT")
            .status(ManifestStatus.DRAFT)
            .build();
        Files.writeString(anotherPath, anotherManifest.toYaml());
        
        registry.scanDirectory(configDir);
        System.out.println("\n=== After Scanning ===");
        System.out.println("Total manifests: " + registry.getAll().size());
        System.out.println("DRAFT manifests: " + registry.getByStatus(ManifestStatus.DRAFT).size());
        
        // ============================================================================
        // 6. Manifest validation
        // ============================================================================
        
        System.out.println("\n=== Validation ===");
        
        // Check dependencies
        for (Dependency dep : manifest.dependencies()) {
            System.out.println("  Dependency: " + dep.id() + " v" + dep.version() + 
                (dep.optional() ? " (optional)" : "") +
                (dep.scope() != null ? " [" + dep.scope() + "]" : ""));
        }
        
        // Check capabilities
        for (ProvidedCapability prov : manifest.provides()) {
            System.out.println("  Provides: " + prov.name() + " (" + prov.type() + ") v" + prov.version());
            if (!prov.features().isEmpty()) {
                System.out.println("    Features: " + String.join(", ", prov.features()));
            }
        }
        
        // Check required capabilities
        for (RequiredCapability req : manifest.requires()) {
            System.out.println("  Requires: " + req.id() + " (" + req.type() + ") v" + req.version() +
                (req.optional() ? " (optional)" : ""));
        }
        
        // Check permissions
        for (Permission perm : manifest.permissions()) {
            System.out.println("  Permission: " + perm.type() + " - " + perm.resource() + ":" + perm.action());
        }
        
        // ============================================================================
        // 7. Manifest evolution (versioning)
        // ============================================================================
        
        System.out.println("\n=== Manifest Evolution ===");
        
        // Update manifest (creates new version)
        Manifest updatedManifest = Manifest.builder()
            .id(manifest.id().asString())
            .name(manifest.name())
            .version("2.1.0") // New version
            .description(manifest.description())
            .mainClass(manifest.mainClass())
            .dependencies(manifest.dependencies())
            .provides(manifest.provides())
            .requires(manifest.requires())
            .permissions(manifest.permissions())
            .configuration(manifest.configuration())
            .authors(manifest.authors())
            .license(manifest.license())
            .repository(manifest.repository())
            .documentation(manifest.documentation())
            .metadata(manifest.metadata())
            .status(ManifestStatus.PUBLISHED)
            .config("llm.defaultModel", "gpt-4-turbo") // New config
            .config("llm.newFeature", true)
            .build();
        
        registry.register(updatedManifest);
        System.out.println("Updated to version: " + updatedManifest.version());
        System.out.println("Total manifests: " + registry.getAll().size());
        
        // Clean up
        Files.deleteIfExists(manifestPath);
        Files.deleteIfExists(anotherPath);
        Files.deleteIfExists(configDir);
    }
}