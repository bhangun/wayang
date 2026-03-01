package tech.kayys.wayang.control.integration.designer;

public record DeploymentResult(
        boolean success,
        String deploymentId,
        String endpoint,
        String error) {
}
