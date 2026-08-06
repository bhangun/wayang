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


import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.core.PermissionType;
import tech.kayys.wayang.core.Version;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.plugin.Manifest;
import tech.kayys.wayang.plugin.ManifestId;
import tech.kayys.wayang.plugin.ManifestStatus;
import tech.kayys.wayang.plugin.ProvidedCapability;
import tech.kayys.wayang.plugin.RequiredCapability;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Default Manifest Implementation
 */
public final class DefaultManifest extends BaseResource implements Manifest {
    
    private final ManifestId id;
    private final String name;
    private final Version version;
    private final String description;
    private final String mainClass;
    private final List<Dependency> dependencies;
    private final List<ProvidedCapability> provides;
    private final List<RequiredCapability> requires;
    private final List<Permission> permissions;
    private final Map<String, Object> configuration;
    private final List<String> authors;
    private final String license;
    private final String repository;
    private final String documentation;
    private final Map<String, String> metadata;
    private final ManifestStatus status;
    private final Path location;
    
    private DefaultManifest(Builder builder) {
        super(
            builder.id != null ? builder.id : new ManifestId(Id.random()),
            builder.metadataMap != null ? Metadata.builder()
                .name(builder.name != null ? builder.name : "manifest-" + builder.id)
                .description(builder.description)
                .version(builder.version != null ? builder.version : Version.VERSION_1_0_0)
                .label("type", "manifest")
                .label("status", builder.status != null ? builder.status.name() : ManifestStatus.DRAFT.name())
                .label("license", builder.license)
                .now()
                .build() : Metadata.builder()
                .name(builder.name != null ? builder.name : "manifest-" + builder.id)
                .version(builder.version != null ? builder.version : Version.VERSION_1_0_0)
                .label("type", "manifest")
                .now()
                .build()
        );
        this.id = (ManifestId) super.id();
        this.name = builder.name;
        this.version = builder.version != null ? builder.version : Version.VERSION_1_0_0;
        this.description = builder.description;
        this.mainClass = builder.mainClass;
        this.dependencies = builder.dependencies != null ? List.copyOf(builder.dependencies) : List.of();
        this.provides = builder.provides != null ? List.copyOf(builder.provides) : List.of();
        this.requires = builder.requires != null ? List.copyOf(builder.requires) : List.of();
        this.permissions = builder.permissions != null ? List.copyOf(builder.permissions) : List.of();
        this.configuration = builder.configuration != null ? Map.copyOf(builder.configuration) : Map.of();
        this.authors = builder.authors != null ? List.copyOf(builder.authors) : List.of();
        this.license = builder.license;
        this.repository = builder.repository;
        this.documentation = builder.documentation;
        this.metadata = builder.metadataMap != null ? Map.copyOf(builder.metadataMap) : Map.of();
        this.status = builder.status != null ? builder.status : ManifestStatus.DRAFT;
        this.location = builder.location;
    }
    
    // === Manifest Interface ===
    
    @Override
    public ManifestId id() {
        return id;
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public Version version() {
        return version;
    }
    
    @Override
    public String description() {
        return description;
    }
    
    @Override
    public String mainClass() {
        return mainClass;
    }
    
    @Override
    public List<Dependency> dependencies() {
        return dependencies;
    }
    
    @Override
    public List<ProvidedCapability> provides() {
        return provides;
    }
    
    @Override
    public List<RequiredCapability> requires() {
        return requires;
    }
    
    @Override
    public List<Permission> permissions() {
        return permissions;
    }
    
    @Override
    public Map<String, Object> configuration() {
        return configuration;
    }
    
    @Override
    public List<String> authors() {
        return authors;
    }
    
    @Override
    public String license() {
        return license;
    }
    
    @Override
    public String repository() {
        return repository;
    }
    
    @Override
    public String documentation() {
        return documentation;
    }
    
    @Override
    public Map<String, String> metadata() {
        return metadata;
    }
    
    @Override
    public ManifestStatus status() {
        return status;
    }
    
    @Override
    public Path location() {
        return location;
    }
    
    @Override
    public Manifest withStatus(ManifestStatus status) {
        return new Builder(this)
            .status(status)
            .build();
    }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Manifest();
    }
    
    @Override
    public ResourceId resourceId() {
        return id;
    }
    
    // === Builder ===
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Manifest fromFile(Path path) throws Exception {
        String fileName = path.getFileName().toString().toLowerCase();
        String content = Files.readString(path);
        
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return fromYaml(content, path);
        } else if (fileName.endsWith(".json")) {
            return fromJson(content, path);
        } else if (fileName.endsWith(".properties")) {
            return fromProperties(content, path);
        } else {
            throw new IllegalArgumentException("Unsupported manifest format: " + fileName);
        }
    }
    
    public static Manifest fromJar(Path jarPath) throws Exception {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            java.util.jar.Manifest manifest = jar.getManifest();
            if (manifest == null) {
                throw new IllegalArgumentException("No manifest in JAR: " + jarPath);
            }
            
            java.util.jar.Attributes attributes = manifest.getMainAttributes();
            
            Builder builder = Manifest.builder();
            
            String id = attributes.getValue("Wayang-Plugin-Id");
            if (id == null) {
                id = jarPath.getFileName().toString().replace(".jar", "");
            }
            builder.id(id);
            
            String name = attributes.getValue("Wayang-Plugin-Name");
            if (name != null) builder.name(name);
            
            String version = attributes.getValue("Wayang-Plugin-Version");
            if (version != null) builder.version(version);
            
            String description = attributes.getValue("Wayang-Plugin-Description");
            if (description != null) builder.description(description);
            
            String mainClass = attributes.getValue("Wayang-Plugin-Main-Class");
            if (mainClass != null) builder.mainClass(mainClass);
            
            String license = attributes.getValue("Wayang-Plugin-License");
            if (license != null) builder.license(license);
            
            String repository = attributes.getValue("Wayang-Plugin-Repository");
            if (repository != null) builder.repository(repository);
            
            builder.location(jarPath);
            builder.status(ManifestStatus.PUBLISHED);
            
            // Parse dependencies if present
            String deps = attributes.getValue("Wayang-Plugin-Dependencies");
            if (deps != null) {
                for (String dep : deps.split(",")) {
                    String[] parts = dep.trim().split(":");
                    if (parts.length >= 1) {
                        builder.dependency(parts[0].trim());
                    }
                }
            }
            
            return builder.build();
        }
    }
    
    public static Manifest fromYaml(String content, Path location) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> data = mapper.readValue(content, Map.class);
        return fromMap(data, location);
    }
    
    public static Manifest fromJson(String content, Path location) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(content, Map.class);
        return fromMap(data, location);
    }
    
    public static Manifest fromProperties(String content, Path location) throws Exception {
        Properties props = new Properties();
        props.load(new StringReader(content));
        Map<String, Object> data = new LinkedHashMap<>();
        for (String key : props.stringPropertyNames()) {
            data.put(key, props.getProperty(key));
        }
        return fromMap(data, location);
    }
    
    @SuppressWarnings("unchecked")
    private static Manifest fromMap(Map<String, Object> data, Path location) {
        Builder builder = Manifest.builder()
            .id((String) data.getOrDefault("id", Id.random().asString()))
            .name((String) data.getOrDefault("name", "unknown"))
            .version((String) data.getOrDefault("version", "1.0.0"))
            .description((String) data.get("description"))
            .mainClass((String) data.get("mainClass"))
            .license((String) data.get("license"))
            .repository((String) data.get("repository"))
            .documentation((String) data.get("documentation"))
            .location(location)
            .status(ManifestStatus.PUBLISHED);
        
        // Authors
        Object authorsObj = data.get("authors");
        if (authorsObj instanceof List) {
            for (Object author : (List<?>) authorsObj) {
                builder.author(author.toString());
            }
        } else if (authorsObj instanceof String) {
            builder.author(authorsObj.toString());
        }
        
        // Dependencies
        Object depsObj = data.get("dependencies");
        if (depsObj instanceof List) {
            for (Object dep : (List<?>) depsObj) {
                if (dep instanceof Map) {
                    Map<String, Object> depMap = (Map<String, Object>) dep;
                    String id = (String) depMap.get("id");
                    String version = (String) depMap.get("version");
                    String scope = (String) depMap.get("scope");
                    Boolean optional = (Boolean) depMap.get("optional");
                    
                    if (id != null) {
                        Dependency dependency = new Dependency(
                            id,
                            version != null ? Version.parse(version) : Version.VERSION_1_0_0,
                            scope != null ? scope : "compile",
                            optional != null && optional
                        );
                        builder.dependency(dependency);
                    }
                } else if (dep instanceof String) {
                    builder.dependency(dep.toString());
                }
            }
        }
        
        // Provides
        Object providesObj = data.get("provides");
        if (providesObj instanceof List) {
            for (Object provide : (List<?>) providesObj) {
                if (provide instanceof Map) {
                    Map<String, Object> provMap = (Map<String, Object>) provide;
                    builder.provides(new ProvidedCapability(
                        (String) provMap.get("id"),
                        (String) provMap.get("name"),
                        provMap.get("version") != null ? Version.parse((String) provMap.get("version")) : Version.VERSION_1_0_0,
                        (String) provMap.get("type"),
                        (String) provMap.get("description"),
                        provMap.get("features") instanceof List ? (List<String>) provMap.get("features") : List.of(),
                        provMap.containsKey("metadata") && provMap.get("metadata") instanceof Map ? 
                            (Map<String, Object>) provMap.get("metadata") : Map.of()
                    ));
                } else if (provide instanceof String) {
                    builder.provides(ProvidedCapability.of(provide.toString(), "extension"));
                }
            }
        }
        
        // Requires
        Object requiresObj = data.get("requires");
        if (requiresObj instanceof List) {
            for (Object require : (List<?>) requiresObj) {
                if (require instanceof Map) {
                    Map<String, Object> reqMap = (Map<String, Object>) require;
                    builder.requires(new RequiredCapability(
                        (String) reqMap.get("id"),
                        reqMap.get("version") != null ? Version.parse((String) reqMap.get("version")) : Version.VERSION_1_0_0,
                        (String) reqMap.get("type"),
                        reqMap.get("optional") instanceof Boolean && (Boolean) reqMap.get("optional"),
                        (String) reqMap.get("description")
                    ));
                } else if (require instanceof String) {
                    builder.requires(RequiredCapability.of(require.toString(), "extension"));
                }
            }
        }
        
        // Permissions
        Object permsObj = data.get("permissions");
        if (permsObj instanceof List) {
            for (Object perm : (List<?>) permsObj) {
                if (perm instanceof Map) {
                    Map<String, Object> permMap = (Map<String, Object>) perm;
                    builder.permission(new Permission(
                        PermissionType.valueOf((String) permMap.getOrDefault("type", "REQUIRED")),
                        (String) permMap.get("resource"),
                        (String) permMap.get("action")
                    ));
                } else if (perm instanceof String) {
                    String[] parts = perm.toString().split(":");
                    if (parts.length == 2) {
                        builder.permission(Permission.of(parts[0], parts[1]));
                    }
                }
            }
        }
        
        // Configuration
        Object configObj = data.get("configuration");
        if (configObj instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) configObj).entrySet()) {
                builder.config(entry.getKey(), entry.getValue());
            }
        }
        
        // Metadata
        Object metaObj = data.get("metadata");
        if (metaObj instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) metaObj).entrySet()) {
                builder.metadata(entry.getKey(), entry.getValue().toString());
            }
        }
        
        return builder.build();
    }
    
    public static class Builder {
        private String id;
        private String name;
        private Version version;
        private String description;
        private String mainClass;
        private final List<Dependency> dependencies = new ArrayList<>();
        private final List<ProvidedCapability> provides = new ArrayList<>();
        private final List<RequiredCapability> requires = new ArrayList<>();
        private final List<Permission> permissions = new ArrayList<>();
        private final Map<String, Object> configuration = new HashMap<>();
        private final List<String> authors = new ArrayList<>();
        private String license;
        private String repository;
        private String documentation;
        private final Map<String, String> metadataMap = new HashMap<>();
        private ManifestStatus status = ManifestStatus.DRAFT;
        private Path location;
        
        public Builder() {}
        
        public Builder(Manifest manifest) {
            this.id = manifest.id().asString();
            this.name = manifest.name();
            this.version = manifest.version();
            this.description = manifest.description();
            this.mainClass = manifest.mainClass();
            this.dependencies.addAll(manifest.dependencies());
            this.provides.addAll(manifest.provides());
            this.requires.addAll(manifest.requires());
            this.permissions.addAll(manifest.permissions());
            this.configuration.putAll(manifest.configuration());
            this.authors.addAll(manifest.authors());
            this.license = manifest.license();
            this.repository = manifest.repository();
            this.documentation = manifest.documentation();
            this.metadataMap.putAll(manifest.metadata());
            this.status = manifest.status();
            this.location = manifest.location();
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder version(Version version) {
            this.version = version;
            return this;
        }
        
        public Builder version(String version) {
            this.version = Version.parse(version);
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }
        
        public Builder dependency(Dependency dependency) {
            this.dependencies.add(dependency);
            return this;
        }
        
        public Builder dependency(String id) {
            this.dependencies.add(new Dependency(id, Version.VERSION_1_0_0, "compile", false));
            return this;
        }
        
        public Builder dependency(String id, Version version) {
            this.dependencies.add(new Dependency(id, version, "compile", false));
            return this;
        }
        
        public Builder dependency(String id, String version) {
            this.dependencies.add(new Dependency(id, Version.parse(version), "compile", false));
            return this;
        }
        
        public Builder dependencies(List<Dependency> dependencies) {
            this.dependencies.addAll(dependencies);
            return this;
        }
        
        public Builder provides(ProvidedCapability capability) {
            this.provides.add(capability);
            return this;
        }
        
        public Builder provides(String id, String type) {
            this.provides.add(ProvidedCapability.of(id, type));
            return this;
        }
        
        public Builder provides(List<ProvidedCapability> capabilities) {
            this.provides.addAll(capabilities);
            return this;
        }
        
        public Builder requires(RequiredCapability capability) {
            this.requires.add(capability);
            return this;
        }
        
        public Builder requires(String id, String type) {
            this.requires.add(RequiredCapability.of(id, type));
            return this;
        }
        
        public Builder requires(List<RequiredCapability> capabilities) {
            this.requires.addAll(capabilities);
            return this;
        }
        
        public Builder permission(Permission permission) {
            this.permissions.add(permission);
            return this;
        }
        
        public Builder permission(String resource, String action) {
            this.permissions.add(Permission.of(resource, action));
            return this;
        }
        
        public Builder permissions(List<Permission> permissions) {
            this.permissions.addAll(permissions);
            return this;
        }
        
        public Builder config(String key, Object value) {
            this.configuration.put(key, value);
            return this;
        }
        
        public Builder configuration(Map<String, Object> configuration) {
            this.configuration.putAll(configuration);
            return this;
        }
        
        public Builder author(String author) {
            this.authors.add(author);
            return this;
        }
        
        public Builder authors(List<String> authors) {
            this.authors.addAll(authors);
            return this;
        }
        
        public Builder license(String license) {
            this.license = license;
            return this;
        }
        
        public Builder repository(String repository) {
            this.repository = repository;
            return this;
        }
        
        public Builder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }
        
        public Builder metadata(String key, String value) {
            this.metadataMap.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, String> metadata) {
            this.metadataMap.putAll(metadata);
            return this;
        }
        
        public Builder status(ManifestStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder location(Path location) {
            this.location = location;
            return this;
        }
        
        public DefaultManifest build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (version == null) {
                version = Version.VERSION_1_0_0;
            }
            return new DefaultManifest(this);
        }
    }
    
    /**
     * Serialize manifest to YAML
     */
    public String toYaml() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> data = toMap();
        return mapper.writeValueAsString(data);
    }
    
    /**
     * Serialize manifest to JSON
     */
    public String toJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = toMap();
        return mapper.writeValueAsString(data);
    }
    
    /**
     * Serialize manifest to Properties
     */
    public String toProperties() throws Exception {
        Properties props = new Properties();
        props.setProperty("id", id.asString());
        props.setProperty("name", name);
        props.setProperty("version", version.toString());
        if (description != null) props.setProperty("description", description);
        if (mainClass != null) props.setProperty("mainClass", mainClass);
        if (license != null) props.setProperty("license", license);
        if (repository != null) props.setProperty("repository", repository);
        if (documentation != null) props.setProperty("documentation", documentation);
        
        // Authors
        for (int i = 0; i < authors.size(); i++) {
            props.setProperty("authors." + i, authors.get(i));
        }
        
        // Dependencies
        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dep = dependencies.get(i);
            props.setProperty("dependencies." + i + ".id", dep.id());
            props.setProperty("dependencies." + i + ".version", dep.version().toString());
        }
        
        // Provides
        for (int i = 0; i < provides.size(); i++) {
            ProvidedCapability prov = provides.get(i);
            props.setProperty("provides." + i + ".id", prov.id());
            props.setProperty("provides." + i + ".type", prov.type());
        }
        
        // Requires
        for (int i = 0; i < requires.size(); i++) {
            RequiredCapability req = requires.get(i);
            props.setProperty("requires." + i + ".id", req.id());
            props.setProperty("requires." + i + ".type", req.type());
        }
        
        StringWriter writer = new StringWriter();
        props.store(writer, "Wayang Manifest");
        return writer.toString();
    }
    
    private Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id.asString());
        data.put("name", name);
        data.put("version", version.toString());
        if (description != null) data.put("description", description);
        if (mainClass != null) data.put("mainClass", mainClass);
        if (license != null) data.put("license", license);
        if (repository != null) data.put("repository", repository);
        if (documentation != null) data.put("documentation", documentation);
        data.put("status", status.name());
        
        // Authors
        if (!authors.isEmpty()) {
            data.put("authors", authors);
        }
        
        // Dependencies
        if (!dependencies.isEmpty()) {
            List<Map<String, Object>> depsList = new ArrayList<>();
            for (Dependency dep : dependencies) {
                Map<String, Object> depMap = new LinkedHashMap<>();
                depMap.put("id", dep.id());
                depMap.put("version", dep.version().toString());
                if (dep.scope() != null) depMap.put("scope", dep.scope());
                depMap.put("optional", dep.optional());
                depsList.add(depMap);
            }
            data.put("dependencies", depsList);
        }
        
        // Provides
        if (!provides.isEmpty()) {
            List<Map<String, Object>> provList = new ArrayList<>();
            for (ProvidedCapability prov : provides) {
                Map<String, Object> provMap = new LinkedHashMap<>();
                provMap.put("id", prov.id());
                provMap.put("name", prov.name());
                provMap.put("version", prov.version().toString());
                provMap.put("type", prov.type());
                if (prov.description() != null) provMap.put("description", prov.description());
                if (!prov.features().isEmpty()) provMap.put("features", prov.features());
                if (!prov.metadata().isEmpty()) provMap.put("metadata", prov.metadata());
                provList.add(provMap);
            }
            data.put("provides", provList);
        }
        
        // Requires
        if (!requires.isEmpty()) {
            List<Map<String, Object>> reqList = new ArrayList<>();
            for (RequiredCapability req : requires) {
                Map<String, Object> reqMap = new LinkedHashMap<>();
                reqMap.put("id", req.id());
                reqMap.put("version", req.version().toString());
                reqMap.put("type", req.type());
                reqMap.put("optional", req.optional());
                if (req.description() != null) reqMap.put("description", req.description());
                reqList.add(reqMap);
            }
            data.put("requires", reqList);
        }
        
        // Permissions
        if (!permissions.isEmpty()) {
            List<Map<String, Object>> permList = new ArrayList<>();
            for (Permission perm : permissions) {
                Map<String, Object> permMap = new LinkedHashMap<>();
                permMap.put("type", perm.type().name());
                permMap.put("resource", perm.resource());
                permMap.put("action", perm.action());
                permList.add(permMap);
            }
            data.put("permissions", permList);
        }
        
        // Configuration
        if (!configuration.isEmpty()) {
            data.put("configuration", configuration);
        }
        
        // Metadata
        if (!metadataMap.isEmpty()) {
            data.put("metadata", metadataMap);
        }
        
        return data;
    }
}
