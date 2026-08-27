package tech.kayys.wayang.provider.routing;

import java.util.List;
import java.util.Optional;

/**
 * Registry managing active model deployments (local runtime instances, cloud provider connections, remote workers).
 */
public interface ModelDeploymentRegistry {

    void register(ModelDeployment deployment);

    void unregister(String deploymentId);

    List<ModelDeployment> findByModel(String modelId);

    List<ModelDeployment> findAvailable(String modelId);

    List<ModelDeployment> listAll();
}
