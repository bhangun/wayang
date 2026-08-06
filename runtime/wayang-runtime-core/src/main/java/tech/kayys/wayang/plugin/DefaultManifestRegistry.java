package tech.kayys.wayang.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.kayys.wayang.plugin.Manifest;
import tech.kayys.wayang.plugin.ManifestId;
import tech.kayys.wayang.plugin.ManifestStatus;
import tech.kayys.wayang.plugin.ProvidedCapability;

/**
 * Default Manifest Registry
 */
public class DefaultManifestRegistry implements ManifestRegistry {
    
    private final Map<ManifestId, Manifest> manifests = new ConcurrentHashMap<>();
    private final Map<String, ManifestId> nameIndex = new ConcurrentHashMap<>();
    private final Map<ManifestStatus, Set<ManifestId>> statusIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<ManifestId>> capabilityIndex = new ConcurrentHashMap<>();
    
    @Override
    public void register(Manifest manifest) {
        manifests.put(manifest.id(), manifest);
        nameIndex.put(manifest.name().toLowerCase(), manifest.id());
        
        statusIndex.computeIfAbsent(manifest.status(), k -> ConcurrentHashMap.newKeySet())
            .add(manifest.id());
        
        for (ProvidedCapability cap : manifest.provides()) {
            capabilityIndex.computeIfAbsent(cap.id(), k -> ConcurrentHashMap.newKeySet())
                .add(manifest.id());
        }
    }
    
    @Override
    public void unregister(ManifestId id) {
        Manifest manifest = manifests.remove(id);
        if (manifest != null) {
            nameIndex.remove(manifest.name().toLowerCase());
            
            Set<ManifestId> statusSet = statusIndex.get(manifest.status());
            if (statusSet != null) {
                statusSet.remove(id);
            }
            
            for (ProvidedCapability cap : manifest.provides()) {
                Set<ManifestId> capSet = capabilityIndex.get(cap.id());
                if (capSet != null) {
                    capSet.remove(id);
                }
            }
        }
    }
    
    @Override
    public Optional<Manifest> get(ManifestId id) {
        return Optional.ofNullable(manifests.get(id));
    }
    
    @Override
    public Optional<Manifest> getByName(String name) {
        ManifestId id = nameIndex.get(name.toLowerCase());
        return id != null ? Optional.ofNullable(manifests.get(id)) : Optional.empty();
    }
    
    @Override
    public List<Manifest> getAll() {
        return new ArrayList<>(manifests.values());
    }
    
    @Override
    public List<Manifest> getByStatus(ManifestStatus status) {
        Set<ManifestId> ids = statusIndex.getOrDefault(status, Set.of());
        return ids.stream()
            .map(manifests::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Manifest> getByCapability(String capability) {
        Set<ManifestId> ids = capabilityIndex.getOrDefault(capability, Set.of());
        return ids.stream()
            .map(manifests::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean exists(ManifestId id) {
        return manifests.containsKey(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return nameIndex.containsKey(name.toLowerCase());
    }
    
    @Override
    public void scanDirectory(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }
        
        try (Stream<Path> stream = Files.walk(directory)) {
            for (Path path : stream.collect(Collectors.toList())) {
                String fileName = path.getFileName().toString().toLowerCase();
                if (Files.isRegularFile(path) && 
                    (fileName.endsWith(".yaml") || fileName.endsWith(".yml") || 
                     fileName.endsWith(".json") || fileName.endsWith(".properties"))) {
                    try {
                        Manifest manifest = DefaultManifest.fromFile(path);
                        register(manifest);
                    } catch (Exception e) {
                        // Log and continue
                        System.err.println("Failed to load manifest: " + path + " - " + e.getMessage());
                    }
                }
            }
        }
    }
    
    public int count() {
        return manifests.size();
    }
    
    public void clear() {
        manifests.clear();
        nameIndex.clear();
        statusIndex.clear();
        capabilityIndex.clear();
    }
}
