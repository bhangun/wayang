package tech.kayys.wayang.integration.designer;

public record DeploymentResult(
        boolean success,
        String deploymentId,
        String endpoint,
        String error) {
}
