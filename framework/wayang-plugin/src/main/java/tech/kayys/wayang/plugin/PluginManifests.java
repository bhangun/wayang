package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.ManifestStatus;

/**
 * Factory helpers for creating plugin manifests.
 */
public final class PluginManifests {

    private PluginManifests() {
    }

    public static Manifest basic(
            String id,
            String name,
            String version,
            String description,
            String mainClass,
            ClassLoader classLoader) {

        return DefaultManifest.builder()
                .id(id)
                .name(name)
                .version(version)
                .description(description)
                .mainClass(mainClass)
                .status(ManifestStatus.PUBLISHED)
                .build();
    }
}
