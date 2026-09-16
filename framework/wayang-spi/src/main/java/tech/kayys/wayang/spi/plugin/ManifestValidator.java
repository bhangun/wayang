package tech.kayys.wayang.spi.plugin;

/**
 * Validates plugin manifests before a plugin is resolved or started.
 */
@FunctionalInterface
public interface ManifestValidator {

    /**
     * Validates a manifest.
     *
     * @param manifest manifest to validate
     * @throws PluginManifestException when the manifest is invalid
     */
    void validate(Manifest manifest);
}
