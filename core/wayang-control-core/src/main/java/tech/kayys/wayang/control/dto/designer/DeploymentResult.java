package tech.kayys.wayang.control.dto.designer;

/**
 * Result of a route design deployment.
 */
public record DeploymentResult(
        String deploymentId,
        boolean success,
        String message,
        long deployedAt) {
}
