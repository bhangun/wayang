package tech.kayys.wayang.spi.plugin;

import java.util.Locale;
import java.util.Objects;
import tech.kayys.wayang.extension.Version;

/**
 * Dependency declared by a Wayang plugin.
 */
public record Dependency(
        String id,
        Version version,
        String scope
) {

    public Dependency {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(version, "version cannot be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
    }

    public static Dependency of(String id, String version) {
        return new Dependency(
                id,
                Version.parse(version),
                DependencyScope.REQUIRED.name());
    }

    public static Dependency of(String id, Version version) {
        return new Dependency(
                id,
                version,
                DependencyScope.REQUIRED.name());
    }

    public static Dependency required(String id, Version version) {
        return new Dependency(
                id,
                version,
                DependencyScope.REQUIRED.name());
    }

    public static Dependency required(String id, String version) {
        return required(id, Version.parse(version));
    }

    public static Dependency optional(String id, Version version) {
        return new Dependency(
                id,
                version,
                DependencyScope.OPTIONAL.name());
    }

    public static Dependency optional(String id, String version) {
        return optional(id, Version.parse(version));
    }

    public static Dependency provided(String id, Version version) {
        return new Dependency(
                id,
                version,
                DependencyScope.PROVIDED.name());
    }

    public static Dependency provided(String id, String version) {
        return provided(id, Version.parse(version));
    }

    public DependencyScope dependencyScope() {
        if (scope == null || scope.isBlank()) {
            return DependencyScope.REQUIRED;
        }

        try {
            return DependencyScope.valueOf(scope.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            /*
             * Backward compatibility:
             * historically null/unknown scope values behaved as ordinary
             * dependencies.
             */
            return DependencyScope.REQUIRED;
        }
    }

    public boolean isRequired() {
        return dependencyScope() == DependencyScope.REQUIRED;
    }

    public boolean isOptional() {
        return dependencyScope() == DependencyScope.OPTIONAL;
    }

    public boolean isProvided() {
        return dependencyScope() == DependencyScope.PROVIDED;
    }
}
