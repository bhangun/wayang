package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.provider.Provider;
import java.time.Instant;
import java.util.Map;

/**
 * Represents a physical, local, or cloud deployment target where a model is executed.
 */
public record ModelDeployment(
        String deploymentId,
        String modelId,
        String providerId,
        Provider providerInstance,
        DeploymentType type,
        String endpoint,
        ModelStatus status,
        Map<String, Object> runtimeConfig,
        Instant registeredAt
) {
    public ModelDeployment {
        status = status != null ? status : ModelStatus.AVAILABLE;
        type = type != null ? type : DeploymentType.CLOUD;
        runtimeConfig = runtimeConfig != null ? Map.copyOf(runtimeConfig) : Map.of();
        registeredAt = registeredAt != null ? registeredAt : Instant.now();
    }

    public static ModelDeployment local(String modelId, Provider provider) {
        return new ModelDeployment("local-" + modelId, modelId, "gollek", provider, DeploymentType.LOCAL, "embedded://gollek", ModelStatus.AVAILABLE, Map.of(), Instant.now());
    }

    public static ModelDeployment cloud(String modelId, String providerId, Provider provider) {
        return new ModelDeployment("cloud-" + providerId + "-" + modelId, modelId, providerId, provider, DeploymentType.CLOUD, "https://api." + providerId + ".com", ModelStatus.AVAILABLE, Map.of(), Instant.now());
    }

    public boolean isAvailable() {
        return status == ModelStatus.AVAILABLE || status == ModelStatus.REGISTERED;
    }
}
