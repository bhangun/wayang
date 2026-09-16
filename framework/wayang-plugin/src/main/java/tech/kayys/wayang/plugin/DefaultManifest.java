package tech.kayys.wayang.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import tech.kayys.wayang.core.Permission;
import tech.kayys.wayang.core.PermissionType;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.spi.plugin.Dependency;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.ManifestId;
import tech.kayys.wayang.spi.plugin.ManifestStatus;
import tech.kayys.wayang.spi.plugin.ProvidedCapability;
import tech.kayys.wayang.spi.plugin.RequiredCapability;

/**
 * Default immutable implementation of Manifest.
 */
public final class DefaultManifest extends BaseResource implements Manifest {

    private final ManifestId id;
    private final String name;
    private final Version version;
    private final Version apiVersion;
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
    private final ManifestStatus status;
    private final Path location;

    private DefaultManifest(Builder builder) {
        super(
                builder.id != null ? builder.id : ManifestId.random(),
                builder.metadata != null ? builder.metadata : Metadata.empty()
        );
        this.id = (ManifestId) super.id();
        this.name = builder.name;
        this.version = builder.version != null ? builder.version : Version.VERSION_1_0_0;
        this.apiVersion = builder.apiVersion != null ? builder.apiVersion : Version.VERSION_1_0_0;
        this.description = builder.description;
        this.mainClass = builder.mainClass;
        this.dependencies = List.copyOf(builder.dependencies);
        this.provides = List.copyOf(builder.provides);
        this.requires = List.copyOf(builder.requires);
        this.permissions = List.copyOf(builder.permissions);
        this.configuration = Map.copyOf(builder.configuration);
        this.authors = List.copyOf(builder.authors);
        this.license = builder.license;
        this.repository = builder.repository;
        this.documentation = builder.documentation;
        this.status = builder.status != null ? builder.status : ManifestStatus.DRAFT;
        this.location = builder.location;
    }

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
    public Version apiVersion() {
        return apiVersion;
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ManifestId id;
        private String name;
        private Version version;
        private Version apiVersion;
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
        private Metadata metadata;
        private ManifestStatus status = ManifestStatus.DRAFT;
        private Path location;

        public Builder() {}

        public Builder(Manifest manifest) {
            this.id = manifest.id();
            this.name = manifest.name();
            this.version = manifest.version();
            this.apiVersion = manifest.apiVersion();
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
            this.metadata = manifest.metadata();
            this.status = manifest.status();
            this.location = manifest.location();
        }

        public Builder id(String id) {
            if (id == null) {
                this.id = null;
                return this;
            }
            try {
                this.id = ManifestId.fromString(id);
            } catch (Exception e) {
                this.id = new ManifestId(new tech.kayys.wayang.extension.Id(
                        java.util.UUID.nameUUIDFromBytes(id.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
            }
            return this;
        }

        public Builder id(ManifestId id) {
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

        public Builder apiVersion(Version apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            this.apiVersion = Version.parse(apiVersion);
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
            this.dependencies.add(Dependency.of(id, Version.VERSION_1_0_0));
            return this;
        }

        public Builder dependency(String id, Version version) {
            this.dependencies.add(Dependency.of(id, version));
            return this;
        }

        public Builder dependency(String id, String version) {
            this.dependencies.add(Dependency.of(id, version));
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

        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
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
                id = ManifestId.random();
            }
            if (version == null) {
                version = Version.VERSION_1_0_0;
            }
            if (apiVersion == null) {
                apiVersion = Version.VERSION_1_0_0;
            }
            return new DefaultManifest(this);
        }
    }
}
