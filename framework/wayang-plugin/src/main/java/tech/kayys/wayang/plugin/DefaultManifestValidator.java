package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.ManifestValidator;
import tech.kayys.wayang.spi.plugin.PluginManifestException;

/**
 * Default validation rules for Wayang plugin manifests.
 */
public final class DefaultManifestValidator implements ManifestValidator {

    @Override
    public void validate(Manifest manifest) {
        if (manifest == null) {
            throw new PluginManifestException(
                    "Plugin manifest cannot be null");
        }

        if (manifest.id() == null) {
            throw new PluginManifestException(
                    "Plugin manifest id cannot be null");
        }

        if (manifest.name() == null || manifest.name().isBlank()) {
            throw new PluginManifestException(
                    "Plugin manifest name cannot be blank");
        }

        if (manifest.version() == null) {
            throw new PluginManifestException(
                    "Plugin manifest version cannot be null");
        }

        if (manifest.apiVersion() == null) {
            throw new PluginManifestException(
                    "Plugin API version cannot be null");
        }

        if (manifest.mainClass() == null
                || manifest.mainClass().isBlank()) {
            throw new PluginManifestException(
                    "Plugin mainClass cannot be blank");
        }

        validateDependencies(manifest);
        validateCapabilities(manifest);
    }

    private void validateDependencies(Manifest manifest) {
        if (manifest.dependencies() == null) {
            return;
        }

        manifest.dependencies().forEach(dependency -> {
            if (dependency == null) {
                throw new PluginManifestException(
                        "Plugin dependency cannot be null");
            }

            if (dependency.id() == null || dependency.id().isBlank()) {
                throw new PluginManifestException(
                        "Plugin dependency id cannot be blank");
            }

            if (dependency.version() == null) {
                throw new PluginManifestException(
                        "Plugin dependency version cannot be null: "
                                + dependency.id());
            }

            dependency.dependencyScope();
        });
    }

    private void validateCapabilities(Manifest manifest) {
        if (manifest.provides() != null) {
            manifest.provides().forEach(capability -> {
                if (capability == null) {
                    throw new PluginManifestException(
                            "Provided capability cannot be null");
                }
            });
        }

        if (manifest.requires() != null) {
            manifest.requires().forEach(capability -> {
                if (capability == null) {
                    throw new PluginManifestException(
                            "Required capability cannot be null");
                }
            });
        }
    }
}
