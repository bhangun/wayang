package tech.kayys.wayang.provider.routing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe default in-memory registry of model deployments.
 */
public class DefaultModelDeploymentRegistry implements ModelDeploymentRegistry {

    private final Map<String, ModelDeployment> deploymentsById = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> deploymentsByModel = new ConcurrentHashMap<>();

    @Override
    public void register(ModelDeployment deployment) {
        if (deployment == null || deployment.deploymentId() == null) return;
        deploymentsById.put(deployment.deploymentId(), deployment);
        if (deployment.modelId() != null) {
            deploymentsByModel.computeIfAbsent(deployment.modelId().toLowerCase(), k -> ConcurrentHashMap.newKeySet())
                    .add(deployment.deploymentId());
        }
    }

    @Override
    public void unregister(String deploymentId) {
        if (deploymentId == null) return;
        ModelDeployment removed = deploymentsById.remove(deploymentId);
        if (removed != null && removed.modelId() != null) {
            Set<String> set = deploymentsByModel.get(removed.modelId().toLowerCase());
            if (set != null) {
                set.remove(deploymentId);
            }
        }
    }

    @Override
    public List<ModelDeployment> findByModel(String modelId) {
        if (modelId == null) return List.of();
        Set<String> depIds = deploymentsByModel.get(modelId.toLowerCase());
        if (depIds == null || depIds.isEmpty()) return List.of();
        return depIds.stream()
                .map(deploymentsById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelDeployment> findAvailable(String modelId) {
        return findByModel(modelId).stream()
                .filter(ModelDeployment::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelDeployment> listAll() {
        return List.copyOf(deploymentsById.values());
    }
}
